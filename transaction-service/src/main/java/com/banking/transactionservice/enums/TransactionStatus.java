package com.banking.transactionservice.enums;

/*
 * Transaction Cycle:
 * 
 * PENDING -> PROCESSING -> COMPLETED (clear transaction)
 * PENDING -> PROCESSING -> PENDING_VERIFICATION (suspicious detected)
 * 						 	-> COMPLETED (if verified)
 * 						 	-> FLAGGED (else SAGA refund)
 * PENDING -> PROCESSING -> FLAGGED
 * PENDING -> PROCESSING -> FAILED
 * 
*/

public enum TransactionStatus {
	
	PENDING,
	PROCESSING,
	COMPLETED,
	PENDING_VERIFICATION,
	FLAGGED,
	FAILED,

}
