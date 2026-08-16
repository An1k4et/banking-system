package com.banking.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import com.banking.transactionservice.enums.TransactionStatus;
import com.banking.transactionservice.enums.TransactionType;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
	
	private String id;
	
	private String senderAccountNumber;
	
	private String receiverAccountNumber;
	
	private BigDecimal amount;
	
	private TransactionType type;
	
	private TransactionStatus status;
	
	private String description;
	
	private String failureReason;
	
	private String refernceNumber;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime completedAt;

}
