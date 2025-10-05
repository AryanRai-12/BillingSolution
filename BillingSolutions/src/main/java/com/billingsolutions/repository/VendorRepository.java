package com.billingsolutions.repository;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    // --- METHODS FROM YOUR OLD REPOSITORY (NOW UPDATED FOR MULTI-TENANCY) ---

    /**
     * UPDATED: Finds a vendor by their party code, but only within a specific business.
     */
    Optional<Vendor> findByPartyCodeAndBusiness(String partyCode, Business business);

    /**
     * UPDATED: Sums the 'due' amount for all vendors (payables), but only for a specific business.
     */
    @Query("SELECT COALESCE(SUM(v.due), 0) FROM Vendor v WHERE v.business = :business")
    BigDecimal sumAllPayablesByBusiness(@Param("business") Business business);

    /**
     * UPDATED & SECURE: Finds a vendor by name, but only within a specific business. 
     * This replaces the old, insecure findByNameIgnoreCase.
     */
    Optional<Vendor> findByNameIgnoreCaseAndBusiness(String name, Business business);

    /**
     * UPDATED & SECURE: Finds vendors with names containing a search query, but only for a specific business.
     * This replaces the old, insecure findByNameContainingIgnoreCase.
     */
    List<Vendor> findByNameContainingIgnoreCaseAndBusiness(String name, Business business);


    // --- NEW METHODS REQUIRED FOR MULTI-TENANCY ---

    /**
     * Finds all vendors that belong to a specific business.
     */
    List<Vendor> findByBusiness(Business business);

    /**
     * Securely finds a single vendor by its ID and business.
     */
    Optional<Vendor> findByIdAndBusiness(Long id, Business business);

    /**
     * Securely checks if a vendor exists for a given business.
     */
    boolean existsByIdAndBusiness(Long id, Business business);
}

