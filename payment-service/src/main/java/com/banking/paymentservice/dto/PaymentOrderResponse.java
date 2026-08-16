package com.banking.paymentservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentOrderResponse {
    private String paymentId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String razorpayKeyId;
    private String status;
}