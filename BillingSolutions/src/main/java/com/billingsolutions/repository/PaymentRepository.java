package com.billingsolutions.repository;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds all payments associated with a specific business.
     * This is crucial for ensuring data isolation in the multi-tenant system.
     * @param business The business to filter payments by.
     * @return A list of payments belonging to the given business.
     */
    List<Payment> findByBusiness(Business business);

}
