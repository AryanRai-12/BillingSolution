package com.billingsolutions.service;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.Business;
import com.billingsolutions.repository.BillRepository;
import com.billingsolutions.repository.CustomerRepository;
import com.billingsolutions.repository.VendorRepository;
import com.billingsolutions.repository.projection.CustomerProfit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

	/**
	 * SECURE: Gets the total profit, but only for the specified business.
	 */
	public BigDecimal getTotalProfit(Business business) { 
		return billRepository.findTotalProfitByBusiness(business); 
	}

	/**
	 * SECURE: Gets the total amount owed by customers, but only for the specified business.
	 */
	public BigDecimal getTotalDuesReceivable(Business business) { 
		return customerRepository.sumAllDuesByBusiness(business); 
	}

	/**
	 * SECURE: Gets the total amount owed to vendors, but only for the specified business.
	 */
	public BigDecimal getTotalVendorPayables(Business business) { 
		return vendorRepository.sumAllPayablesByBusiness(business); 
	}

	/**
	 * SECURE: Gets the top customers by profit, but only within the specified business.
	 */
	public List<CustomerProfit> getTopCustomersByProfit(int limit, Business business) {
		List<CustomerProfit> all = billRepository.findCustomerProfitLeaderboardByBusiness(business);
		return all.size() > limit ? all.subList(0, limit) : all;
	}

	/**
	 * SECURE: Gets the most recent bills, but only for the specified business.
	 */
	public List<Bill> getRecentBills(int limit, Business business) {
		// Uses pagination to efficiently get the most recent bills without loading all of them.
		PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
		return billRepository.findByBusiness(business, pageRequest).getContent();
	}
}
