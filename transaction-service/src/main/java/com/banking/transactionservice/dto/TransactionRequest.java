package com.banking.transactionservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
	
	@NotBlank(message = "Sender account number is require")
	private String senderAccountNumber;
	
	@NotBlank(message = "Receiver account number is require")
	private String receiverAccountNumber;
	
	@NotBlank(message = "Amount is require")
	@Positive(message = "Amount must be positive")
	private BigDecimal amount;
	
	private String description;

}
