package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEventConsumer {
	
	private final AccountService accountService;
	
	/*
	 * Consume transaction.completed event from kafka
	 * Credit recieve account
	*/
	@KafkaListener(topics = "trasaction.completed")
	public void consumeTransactionCompleted(
			@Payload Map<String, String> payload
	) {
		
		try {
			
			String receive = (String) payload.get("receiveAccountNumber");
			BigDecimal amount = new BigDecimal(payload.get("amount").toString());
			
			log.info("Crediting account: {} amount: {}", receive, amount);
			accountService.creditBalance(receive, amount);
			
		}
		catch(Exception e) {
			log.error("Error crediting account: {}", e.getMessage());
		}
		
	}
	
	/*
	 * Consume fraud.detected event from kafka
	 * Block the account
	*/
	@KafkaListener(topics = "fraud.detected")
	public void consumeFraudDetected(
			@Payload Map<String, String> payload
	) {
		try {
			
			String accountNumber = (String) payload.get("accountNumber");
			
			log.info("Fraud detected. blocking account {}", accountNumber);
			accountService.blockAccount(accountNumber);
			
		}
		catch(Exception e) {
			log.error("Error blocking account: {}", e.getMessage());
		}
	}

}
