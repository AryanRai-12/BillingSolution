package com.billingsolutions.repository;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // --- METHODS FROM YOUR OLD REPOSITORY (NOW UPDATED FOR MULTI-TENANCY) ---

    /**
     * UPDATED: Finds a customer by their party code, but only within a specific business.
     * This prevents party code conflicts between different businesses.
     */
    Optional<Customer> findByPartyCodeAndBusiness(String partyCode, Business business);

    /**
     * UPDATED: Sums the 'due' amount for all customers, but only for a specific business.
     * This is a critical security and data-integrity update.
     */
    @Query("SELECT COALESCE(SUM(c.due), 0) FROM Customer c WHERE c.business = :business")
    BigDecimal sumAllDuesByBusiness(@Param("business") Business business);


    // --- NEW METHODS REQUIRED FOR MULTI-TENANCY ---

    /**
     * Finds all customers that belong to a specific business.
     */
    List<Customer> findByBusiness(Business business);

    /**
     * Securely finds a single customer by its ID and business.
     */
    Optional<Customer> findByIdAndBusiness(Long id, Business business);
    
    /**
     * Securely checks if a customer exists for a given business.
     */
    boolean existsByIdAndBusiness(Long id, Business business);
}

