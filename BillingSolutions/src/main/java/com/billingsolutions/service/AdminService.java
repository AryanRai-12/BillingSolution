package com.billingsolutions.service;

import com.billingsolutions.model.Bill;
import com.billingsolutions.repository.BillRepository;
import com.billingsolutions.repository.CustomerRepository;
import com.billingsolutions.repository.VendorRepository;
import com.billingsolutions.repository.projection.CustomerProfit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminService {
	private final BillRepository billRepository;
	private final CustomerRepository customerRepository;
	private final VendorRepository vendorRepository;

	public AdminService(BillRepository billRepository, CustomerRepository customerRepository, VendorRepository vendorRepository) {
		this.billRepository = billRepository;
		this.customerRepository = customerRepository;
		this.vendorRepository = vendorRepository;
	}

	public BigDecimal getTotalProfit() { return billRepository.findTotalProfit(); }
	public BigDecimal getTotalDuesReceivable() { return customerRepository.sumAllDues(); }
	public BigDecimal getTotalVendorPayables() { return vendorRepository.sumAllPayables(); }
	public List<CustomerProfit> getTopCustomersByProfit(int limit) {
		List<CustomerProfit> all = billRepository.findCustomerProfitLeaderboard();
		return all.size() > limit ? all.subList(0, limit) : all;
	}
	public List<Bill> getRecentBills(int limit) {
		List<Bill> all = billRepository.findAll();
		return all.size() > limit ? all.subList(Math.max(0, all.size() - limit), all.size()) : all;
	}
} 