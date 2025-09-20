package com.billingsolutions.repository;

import com.billingsolutions.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Optional<Customer> findByPartyCode(String partyCode);

	@Query("select coalesce(sum(c.due), 0) from Customer c")
	BigDecimal sumAllDues();
} 