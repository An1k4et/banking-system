package com.banking.transactionservice.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.enums.TransactionStatus;
import com.banking.transactionservice.enums.TransactionType;
import com.banking.transactionservice.event.TransactionCompletedEvent;
import com.banking.transactionservice.event.TransactionInitiatedEvent;
import com.banking.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
	
	private final TransactionRepository transactionRepository;
	private final AccountServiceClient accountServiceClient;
	
	private final RedisTemplate<String, String> redisTemplate;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	private static final String TRANSACTION_INTIATED_TOPIC = "transaction.initiated";
	private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
	private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";
	private static final String FRAUD_DETECTED_TOPIC = "fraud.detected";
	
	/*
	 * SAGA Step 1: Initiate transfer
	 * Deduct from sender via feign
	 * saves transaction as PROCESSING
	 * publish event to kafka for fraud check 
	 * returns
	*/
	public TransactionResponse transfer(TransactionRequest request) {
		
		log.info("SAGA start - Transfer: {} -> {} amount: {}", request.getSenderAccountNumber(), request.getReceiverAccountNumber(), request.getAmount());
		
		// SAGA step1: Deduct from sender
		accountServiceClient.deductBalance(request.getSenderAccountNumber(), request.getAmount());
		
		Transaction transaction = Transaction.builder()
											.senderAccountNumber(request.getSenderAccountNumber())
											.receiverAccountNumber(request.getReceiverAccountNumber())
											.amount(request.getAmount())
											.type(TransactionType.TRANSFER)
											.status(TransactionStatus.PROCESSING)
											.description(request.getDescription())
											.refernceNumber(UUID.randomUUID().toString())
											.build();
		Transaction savedTransaction = transactionRepository.save(transaction);
		log.info("Transaction saved as PROCESSING: {}", savedTransaction.getId());
		
		//SAGA step 2: Publish for fraud check
		TransactionInitiatedEvent event = TransactionInitiatedEvent.builder()
																.transactionId(savedTransaction.getId())
																.senderAccountNumber(savedTransaction.getSenderAccountNumber())
																.receiverAccountNumber(savedTransaction.getReceiverAccountNumber())
																.amount(savedTransaction.getAmount())
																.description(savedTransaction.getDescription())
																.build();
		
		kafkaTemplate.send(TRANSACTION_INTIATED_TOPIC, savedTransaction.getId(), event);
		log.info("SAGA step 2: - TransactionInitiatedEvent: {}", savedTransaction.getId());
		
		return mapToResponse(savedTransaction);
	}

	private TransactionResponse mapToResponse(Transaction transaction) {
		return TransactionResponse.builder()
	            .id(transaction.getId())
	            .senderAccountNumber(transaction.getSenderAccountNumber())
	            .receiverAccountNumber(transaction.getReceiverAccountNumber())
	            .amount(transaction.getAmount())
	            .type(transaction.getType())
	            .status(transaction.getStatus())
	            .description(transaction.getDescription())
	            .failureReason(transaction.getFailureReason())
	            .refernceNumber(transaction.getRefernceNumber())
	            .createdAt(transaction.getCreatedAt())
	            .completedAt(transaction.getCompletedAt())
	            .build();
	}

	public TransactionResponse getTransaction(String transactionId) {
		return mapToResponse(transactionRepository.findById(transactionId)
											.orElseThrow(() -> new RuntimeException("TransactionId not fount: "+transactionId)));
	}

	public List<TransactionResponse> getTransactionHistory(String accountNumber) {
		
		return transactionRepository.findBySenderAccountNumberOrderByCreatedAtDesc(accountNumber)
				                  .stream()
				                  .map(this::mapToResponse)
				                  .collect(Collectors.toList());
	}
	
	public TransactionResponse verifyOtp(String transactionID, String otp){
		
		log.info("OTP verification for the transaction: {}", transactionID);

        Transaction transaction = transactionRepository.findById(transactionID)
                .orElseThrow(() -> new RuntimeException("Transaction not found "+transactionID));
        
        String otpKey = "verification:otp" + transactionID;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        
        // OTP is EXPIRED
        if(storedOtp == null){
            log.warn("OTP expired for transaction: {}", transactionID);
            compensateTransaction(transaction, "OTP expired - transaction cancelled and amount refunded");
            return mapToResponse(transaction);
        }
        
        // BLOCK ACCOUNT AND REFUND
        if(!storedOtp.equals(otp)){
            log.warn("Wrong OTP - blocking account and refunding: {}", transactionID);
            redisTemplate.delete(otpKey);
            blockAccountAndCompensate(transaction, "Wrong OTP entered - transaction cancelled, " + "Account has been blocked for security");
            return mapToResponse(transaction);
        }
        
        // OTP correct - complete transaction
        log.info("OTP verified - completing transaction: {}", transactionID);
        redisTemplate.delete(otpKey);
        completeTransaction(transaction);
        return mapToResponse(transaction);
        
	}

	private void completeTransaction(Transaction transaction) {
		
		transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        
        TransactionCompletedEvent completedEvent = TransactionCompletedEvent.builder()
                .transactionId(transaction.getId())
                .senderAccountNumber(transaction.getSenderAccountNumber())
                .receiverAccountNumber(transaction.getReceiverAccountNumber())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .build();
        
        kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC, transaction.getId(), completedEvent);

        log.info("SAGA COMPLETE - Transaction {} completed", transaction.getId());
		
	}

	private void blockAccountAndCompensate(Transaction transaction, String reason) {
		// Publish fraud.detected -> Account Service will block account
        Map<String, Object> fraudEvent = new HashMap<>();
        fraudEvent.put("transactionId", transaction.getId());
        fraudEvent.put("accountNumber", transaction.getSenderAccountNumber());
        fraudEvent.put("reason", reason);
        
        kafkaTemplate.send(FRAUD_DETECTED_TOPIC, transaction.getSenderAccountNumber(), fraudEvent);
        log.warn("fraud.detected published - account: {} will be blocked, Kindly contact to the bank", transaction.getSenderAccountNumber());
        
        // SAGA COMPENSATION - refund Sender
        compensateTransaction(transaction, reason);
		
	}

	private void compensateTransaction(Transaction transaction, String reason) {
		
		log.warn("SAGA COMPENSATION - refunding: {} amount: {}", transaction.getSenderAccountNumber(), transaction.getAmount());
		
		// CREDIT MONEY BACK TO SENDER SYNCHRONOUSLY
        accountServiceClient.creditBalance(transaction.getSenderAccountNumber(), transaction.getAmount());

        transaction.setStatus(TransactionStatus.FLAGGED);
        transaction.setFailureReason(reason + " - SAGA Compensation executed, amount refunded at "+ LocalDateTime.now());
        
        transactionRepository.save(transaction);

        // PUBLISH refund event - Notification service will alert user
        Map<String, Object> refundEvent = new HashMap<>();
        refundEvent.put("transactionId", transaction.getId());
        refundEvent.put("senderAccountNumber", transaction.getSenderAccountNumber());
        refundEvent.put("amount", transaction.getAmount());
        refundEvent.put("reason", reason);
        
        kafkaTemplate.send(TRANSACTION_REFUNDED_TOPIC, transaction.getId(), refundEvent);
        
        log.info("SAGA COMPENSATION COMPLETE - {} refunded to  {}", transaction.getAmount(), transaction.getSenderAccountNumber());
		
	}

	public void processCleanResult(String transactionId) {
		
		Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found "+transactionId));
		
		if(transaction.getStatus() != TransactionStatus.PROCESSING){
            log.warn("Transaction {} not PROCESSING - skipping", transactionId);
            return;
        }

        completeTransaction(transaction);
		
	}
	

}
