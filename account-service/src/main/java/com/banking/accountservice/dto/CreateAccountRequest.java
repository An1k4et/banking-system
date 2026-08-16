package com.banking.accountservice.dto;

import java.math.BigDecimal;

import com.banking.accountservice.enums.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
	
	@NotBlank(message = "Account Holder Name required")
	private String accountHolderName;
	
	@NotBlank(message = "Email required")
	@Email(message = "Invalid email format")
	private String email;
	
	@NotBlank(message = "Phone is required")
	private String phone;
	
	@NotBlank(message = "Account Type is required")
	private AccountType accountType;
	
	@NotBlank(message = "Initial Deposit is required")
	@Positive(message = "Inital Deposit must be positive")
	private BigDecimal initialDeposit;

}
