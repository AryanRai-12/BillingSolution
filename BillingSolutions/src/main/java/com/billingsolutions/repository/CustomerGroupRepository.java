package com.billingsolutions.repository;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
    

    /**
     * Finds all customer groups for a specific business.
     * @param business The current business.
     * @return A list of customer groups.
     */
    List<CustomerGroup> findByBusinessOrderByName(Business business);
	
	
	Optional<CustomerGroup> findByNameAndBusiness(String name, Business business);
    // You already have this one
    List<CustomerGroup> findByBusinessOrderByNameAsc(Business business);

    // ADD THIS METHOD: Finds a group by ID only if it belongs to the specified business.
    Optional<CustomerGroup> findByIdAndBusiness(Long id, Business business);

    // ADD THIS METHOD: Checks for existence by ID only if it belongs to the specified business.
    boolean existsByIdAndBusiness(Long id, Business business);
}