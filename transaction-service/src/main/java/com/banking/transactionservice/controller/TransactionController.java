package com.banking.transactionservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
	
	private TransactionService transactionService;
	
	@PostMapping("/transfer")
	public ResponseEntity<TransactionResponse> transfer(
			@Valid @RequestBody TransactionRequest request
	){
		return ResponseEntity.status(HttpStatus.CREATED)
							.body(transactionService.transfer(request));
	}
	
	@GetMapping("/{transactionId}")
	public ResponseEntity<TransactionResponse> getTransaction(
			@PathVariable String transactionId
	){
		return ResponseEntity.ok(transactionService.getTransaction(transactionId));
	}
	
	@GetMapping("/{accountNumber}")
	public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
			@PathVariable String accountNumber
	){
		return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));
	}
	
	@PostMapping("/{transactionId}/verify")
	public ResponseEntity<TransactionResponse> verifyOtp(
			@PathVariable String transactionId,
			@RequestParam String otp
	){
		log.info("OTP verfification request - trasnaction: {}", transactionId);
		
		return ResponseEntity.ok(transactionService.verifyOtp(transactionId,otp));
		
	}

}
