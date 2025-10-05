package com.billingsolutions.service;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.User;
import com.billingsolutions.model.Vendor;
import com.billingsolutions.repository.UserRepository;
import com.billingsolutions.repository.VendorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VendorService {
	private final VendorRepository vendorRepository;
	private final UserRepository userRepository;

	public VendorService(VendorRepository vendorRepository, UserRepository userRepository) {
		this.vendorRepository = vendorRepository;
		this.userRepository = userRepository;
	}

	/**
     * Helper method to securely get the Business of the currently authenticated user.
     */
    private Business getCurrentBusiness() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user '" + currentUsername + "' not found in database."));
        
        Business business = currentUser.getBusiness();
        if (business == null) {
            throw new IllegalStateException("User '" + currentUsername + "' is not associated with any business.");
        }
        return business;
    }

	public List<Vendor> findAll() { 
		return vendorRepository.findByBusiness(getCurrentBusiness()); 
	}

	public Vendor findById(Long id) { 
		return vendorRepository.findByIdAndBusiness(id, getCurrentBusiness())
			.orElseThrow(() -> new EntityNotFoundException("Vendor not found with ID: " + id));
	}

	public Optional<Vendor> findByPartyCode(String partyCode) { 
		return vendorRepository.findByPartyCodeAndBusiness(partyCode, getCurrentBusiness()); 
	}

	public Vendor save(Vendor vendor) { 
		if (vendor.getId() == null) {
			vendor.setBusiness(getCurrentBusiness());
		}
		return vendorRepository.save(vendor); 
	}

	public void deleteById(Long id) { 
		Business currentBusiness = getCurrentBusiness();
		if (!vendorRepository.existsByIdAndBusiness(id, currentBusiness)) {
			throw new EntityNotFoundException("Vendor not found with ID: " + id);
		}
		vendorRepository.deleteById(id); 
	}

	public void updateDue(Long vendorId, BigDecimal delta) {
		Vendor vendor = findById(vendorId); // This call is now secure
		vendor.setDue(vendor.getDue().add(delta));
		vendorRepository.save(vendor);
	}

	public BigDecimal sumAllPayablesForCurrentBusiness() {
		return vendorRepository.sumAllPayablesByBusiness(getCurrentBusiness());
	}

	/**
	 * ADDED: Secure method to find a single vendor by name for the current business.
	 * This is called when creating/updating a product.
	 */
	public Optional<Vendor> findByNameIgnoreCase(String name) {
		return vendorRepository.findByNameIgnoreCaseAndBusiness(name, getCurrentBusiness());
	}

	/**
	 * ADDED: Secure method to search for vendors by name for the current business.
	 * This is used by the search dropdown on the product form.
	 */
	public List<Vendor> searchByName(String query) {
		return vendorRepository.findByNameContainingIgnoreCaseAndBusiness(query, getCurrentBusiness());
	}
}

