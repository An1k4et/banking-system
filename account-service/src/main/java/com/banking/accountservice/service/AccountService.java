package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.enums.AccountStatus;
import com.banking.accountservice.enums.AccountType;
import com.banking.accountservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
	
	private AccountRepository accountRespository;
	private static SecureRandom secureRandom = new SecureRandom();
	
	public AccountResponse createAccount(CreateAccountRequest request) {
		
		log.info("Creating account for {}", request.getEmail());
		
		if(accountRespository.existByEmail(request.getEmail())) {
			throw new RuntimeException("Account lready exist for email: "+request.getEmail());
		}
		
		Account account = Account.builder()
								.accountHolderName(request.getAccountHolderName())
								.accountType(request.getAccountType())
								.balance(request.getInitialDeposit())
								.email(request.getEmail())
								.phone(request.getPhone())
								.status(AccountStatus.ACTIVE)
								.accountNumber(generateAccountNumber())
								.dailyTransactionLimit(
										request.getAccountType() == AccountType.SAVING
										? new BigDecimal("100000")
										: new BigDecimal("500000")
								)
								.build();
		Account savedAccount = accountRespository.save(account);
		log.info("Account Created {}", savedAccount.getAccountNumber());
		return mapToResponse(savedAccount);
	}

	// Genarate 12 digit unique Account number
	private String generateAccountNumber() {
		String accountNumber;
		do {
			
			long number = secureRandom.nextLong(1_000_000_000_000L);
			accountNumber = String.format("%012d", number);
			
		}while(accountRespository.existByAccountNumber(accountNumber));
		return accountNumber;
	}
	
	// Get Account by Account number
	public AccountResponse getAccount(String accountNumber) {
		Account account = accountRespository.findByAccountNumber(accountNumber)
							.orElseThrow(() -> new RuntimeException("Account Not Found"));
		return mapToResponse(account);
	}

	// Get balance by Account number
	public BigDecimal getBalance(String accountNumber) {
		Account account = accountRespository.findByAccountNumber(accountNumber)
							.orElseThrow(() -> new RuntimeException("Account Not Found"));
		return account.getBalance();
	}
	
	/*
	 * Block Account called by Apache Kafka 
	*/
	public void blockAccount(String accountNumber) {
		log.info("Blocking Account: {}", accountNumber);
		Account account = accountRespository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account Not Found"));
		account.setStatus(AccountStatus.BLOCKED);
		accountRespository.save(account);
		log.info("Account Blocked: {}", accountNumber);
	}
	
	/*
	 * Deduct balance called by transaction service 
	*/
	public void deductBalance(String accountNumber, BigDecimal amount) {
		log.info("Deducting balance {} from acount {}", amount, accountNumber);
		Account account = accountRespository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account Not Found"));
		
		if(account.getStatus() != AccountStatus.ACTIVE) {
			throw new RuntimeException("Account is not active "+accountNumber);
		}
		
		if(account.getBalance().compareTo(amount) < 0) {
			throw new RuntimeException("insufficient balance for account "+accountNumber);
		}
		
		account.setBalance(account.getBalance().subtract(amount));
		accountRespository.save(account);
		
		log.info("Balance updated. New Balance {}", accountNumber);
	}
	
	/*
	 * Credit balance called by transaction service 
	*/
	public void creditBalance(String accountNumber, BigDecimal amount) {
		log.info("Crediting balance {} to acount {}", amount, accountNumber);
		Account account = accountRespository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account Not Found"));
		
		account.setBalance(account.getBalance().add(amount));
		accountRespository.save(account);
		
		log.info("Balance credited. New Balance {}", accountNumber);
	}

	// Mapper to convert into dto
	private AccountResponse mapToResponse(Account savedAccount) {
		AccountResponse response = AccountResponse.builder()
												.id(savedAccount.getId())
												.accountNumber(savedAccount.getAccountNumber())
												.accountHolderName(savedAccount.getAccountHolderName())
												.email(savedAccount.getEmail())
												.phone(savedAccount.getPhone())
												.accountType(savedAccount.getAccountType())
												.status(savedAccount.getStatus())
												.balance(savedAccount.getBalance())
												.dailyTransactionLimit(savedAccount.getDailyTransactionLimit())
												.createdAt(savedAccount.getCreatedAt())
												.build();
		return response;
	}

}
