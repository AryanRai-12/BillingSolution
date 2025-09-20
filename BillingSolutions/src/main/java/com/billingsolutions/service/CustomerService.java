package com.billingsolutions.service;

import com.billingsolutions.model.Customer;
import com.billingsolutions.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	public List<Customer> findAll() { return customerRepository.findAll(); }
	public Optional<Customer> findById(Long id) { return customerRepository.findById(id); }
	public Optional<Customer> findByPartyCode(String partyCode) { return customerRepository.findByPartyCode(partyCode); }

	@Transactional
	public Customer save(Customer customer) {
		return customerRepository.save(customer);
	}

	@Transactional
	public void deleteById(Long id) {
		customerRepository.deleteById(id);
	}
	
	
	@Transactional
	public void updateDue(Long customerId, BigDecimal delta) {
		Customer customer = customerRepository.findById(customerId).orElseThrow();
		customer.setDue(customer.getDue().add(delta));
		customerRepository.save(customer);
	}
} 