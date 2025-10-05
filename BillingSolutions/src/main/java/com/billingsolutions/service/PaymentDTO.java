package com.billingsolutions.service;

import com.billingsolutions.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A simple DTO to hold the details of a single payment for display.
 */
public class PaymentDTO {

    private final BigDecimal amount;
    private final LocalDateTime paymentDate;
    private final String receivedBy;

    public PaymentDTO(Payment payment) {
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
        this.receivedBy = payment.getReceivedBy().getUsername();
    }

    // Standard Getters
    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public String getReceivedBy() {
        return receivedBy;
    }
}
