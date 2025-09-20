package com.billingsolutions.service;

import com.billingsolutions.model.Vendor;
import com.billingsolutions.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class VendorService {
	private final VendorRepository vendorRepository;

	public VendorService(VendorRepository vendorRepository) {
		this.vendorRepository = vendorRepository;
	}

	public List<Vendor> findAll() { return vendorRepository.findAll(); }
	public Optional<Vendor> findById(Long id) { return vendorRepository.findById(id); }
	public Optional<Vendor> findByPartyCode(String partyCode) { return vendorRepository.findByPartyCode(partyCode); }

	@Transactional
	public Vendor save(Vendor vendor) { return vendorRepository.save(vendor); }

	@Transactional
	public void deleteById(Long id) { vendorRepository.deleteById(id); }

	@Transactional
	public void updateDue(Long vendorId, BigDecimal delta) {
		Vendor vendor = vendorRepository.findById(vendorId).orElseThrow();
		vendor.setDue(vendor.getDue().add(delta));
		vendorRepository.save(vendor);
	}
} 