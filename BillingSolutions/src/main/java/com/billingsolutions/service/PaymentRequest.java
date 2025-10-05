package com.billingsolutions.service;

import java.math.BigDecimal;

public class PaymentRequest {
    private Long billId;
    private String paymentType; // Will be "none", "full", or "partial"
    private BigDecimal partialAmount;

    // Getters and Setters
    public Long getBillId() { 
        return billId; 
    }
    public void setBillId(Long billId) { 
        this.billId = billId; 
    }
    public String getPaymentType() { 
        return paymentType; 
    }
    public void setPaymentType(String paymentType) { 
        this.paymentType = paymentType; 
    }
    public BigDecimal getPartialAmount() { 
        return partialAmount; 
    }
    public void setPartialAmount(BigDecimal partialAmount) { 
        this.partialAmount = partialAmount; 
    }
}

