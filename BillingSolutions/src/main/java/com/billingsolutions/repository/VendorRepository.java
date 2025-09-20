package com.billingsolutions.repository;

import com.billingsolutions.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
	Optional<Vendor> findByPartyCode(String partyCode);

	@Query("select coalesce(sum(v.due), 0) from Vendor v")
	BigDecimal sumAllPayables();
} 