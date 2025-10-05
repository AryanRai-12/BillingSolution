//package com.billingsolutions.service;
//
//import com.billingsolutions.model.Bill;
//import java.math.BigDecimal;
//
///**
// * A simple DTO to securely send a bill's payment summary to the frontend.
// */
//public class BillPaymentDetailsDTO {
//
//    private final BigDecimal total;
//    private final BigDecimal amountPaid;
//    private final BigDecimal remainingDue;
//
//    public BillPaymentDetailsDTO(Bill bill) {
//        this.total = bill.getTotal();
//        // We use the bill's 'paymentAgainstPreviousDue' field, which tracks the sum of payments.
//        this.amountPaid = bill.getPaymentAgainstPreviousDue();
//        // The remaining due is the bill's total minus the amount already paid.
//        this.remainingDue = this.total.subtract(this.amountPaid);
//    }
//
//    // Standard Getters
//    public BigDecimal getTotal() {
//        return total;
//    }
//
//    public BigDecimal getAmountPaid() {
//        return amountPaid;
//    }
//
//    public BigDecimal getRemainingDue() {
//        return remainingDue;
//    }
//}



package com.billingsolutions.service;

import com.billingsolutions.model.Bill;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple DTO to securely send a bill's payment summary and history to the frontend.
 */
public class BillPaymentDetailsDTO {

    private final BigDecimal total;
    private final BigDecimal amountPaid;
    private final BigDecimal remainingDue;
    // ADDED: A list to hold the details of all previous payments for this bill.
    private final List<PaymentDTO> previousPayments;

    public BillPaymentDetailsDTO(Bill bill) {
        this.total = bill.getTotal();
        this.amountPaid = bill.getPaymentAgainstPreviousDue();
        this.remainingDue = this.total.subtract(this.amountPaid);
        
        // ADDED: This logic populates the list of previous payments from the bill's payment list.
        this.previousPayments = bill.getPayments().stream()
                                    .map(PaymentDTO::new)
                                    .collect(Collectors.toList());
    }

    // Standard Getters
    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getRemainingDue() {
        return remainingDue;
    }

    // ADDED: A getter for the new list of previous payments.
    public List<PaymentDTO> getPreviousPayments() {
        return previousPayments;
    }
}

