package com.billingsolutions.service;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.Business;
import com.billingsolutions.model.Customer;
import com.billingsolutions.model.Payment;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.BillRepository;
import com.billingsolutions.repository.CustomerRepository;
import com.billingsolutions.repository.PaymentRepository;
import com.billingsolutions.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          BillRepository billRepository,
                          CustomerRepository customerRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    private Business getCurrentBusiness() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user '" + currentUsername + "' not found in database."));
        
        Business business = currentUser.getBusiness();
        if (business == null) {
            throw new IllegalStateException("User '" + currentUsername + "' is not associated with any business.");
        }
        return business;
    }
    
    public Payment createPayment(Long billId, BigDecimal amount) {
        Business currentBusiness = getCurrentBusiness();
        User currentUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();

        Bill bill = billRepository.findByIdAndBusiness(billId, currentBusiness)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + billId));

        Customer customer = bill.getCustomer();

        Payment payment = new Payment();
        payment.setBusiness(currentBusiness);
        payment.setBill(bill);
        payment.setAmount(amount);
        payment.setReceivedBy(currentUser);
        paymentRepository.save(payment);

        // THE FIX: Check for null before performing calculations.
        BigDecimal currentCustomerDue = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;
        customer.setDue(currentCustomerDue.subtract(amount));
        
        BigDecimal currentBillPayment = bill.getPaymentAgainstPreviousDue() != null ? bill.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
        bill.setPaymentAgainstPreviousDue(currentBillPayment.add(amount));

        // Note: The explicit .save() calls are not necessary inside a @Transactional method
        // for managed entities, but they do not cause harm.
        customerRepository.save(customer);
        billRepository.save(bill);

        return payment;
    }

    public void deletePayment(Long paymentId) {
        Business currentBusiness = getCurrentBusiness();
        Payment payment = paymentRepository.findById(paymentId)
                .filter(p -> p.getBusiness().getId().equals(currentBusiness.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with ID: " + paymentId));

        Bill bill = payment.getBill();
        Customer customer = bill.getCustomer();
        BigDecimal paymentAmount = payment.getAmount();

        // THE FIX: Check for null before performing calculations.
        BigDecimal currentCustomerDue = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;
        customer.setDue(currentCustomerDue.add(paymentAmount));
        
        BigDecimal currentBillPayment = bill.getPaymentAgainstPreviousDue() != null ? bill.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
        bill.setPaymentAgainstPreviousDue(currentBillPayment.subtract(paymentAmount));

        customerRepository.save(customer);
        billRepository.save(bill);

        paymentRepository.delete(payment);
    }

    public List<Payment> findAllForCurrentBusiness() {
        return paymentRepository.findByBusiness(getCurrentBusiness());
    }
}

