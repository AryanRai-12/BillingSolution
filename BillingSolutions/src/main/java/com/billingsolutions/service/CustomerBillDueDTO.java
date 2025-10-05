package com.billingsolutions.service;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.Payment;
import java.math.BigDecimal;

public class CustomerBillDueDTO {
    private Long billId;
    private String billNo;
    private BigDecimal remainingDue;

    public CustomerBillDueDTO(Bill bill) {
        this.billId = bill.getId();
        this.billNo = bill.getBillNo();
        
        // Calculate the remaining due for this specific bill by summing its payments
        BigDecimal amountPaid = bill.getPayments().stream()
                                    .map(Payment::getAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.remainingDue = bill.getTotal().subtract(amountPaid);
    }

    // Getters
    public Long getBillId() { return billId; }
    public String getBillNo() { return billNo; }
    public BigDecimal getRemainingDue() { return remainingDue; }
}

