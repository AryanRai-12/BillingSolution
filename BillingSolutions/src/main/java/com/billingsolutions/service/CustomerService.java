package com.billingsolutions.service;

// ADDED: Imports for new multi-tenancy logic
import com.billingsolutions.model.Business;
import com.billingsolutions.model.Customer;
import com.billingsolutions.model.CustomerGroup;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.CustomerGroupRepository;
import com.billingsolutions.repository.CustomerRepository;
// ADDED: UserRepository is needed to find the current user's business
import com.billingsolutions.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
// ADDED: Spring Security classes to get the currently logged-in user
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
// MODIFIED: Added @Transactional to the class level for consistency
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;
    // ADDED: UserRepository dependency
    private final UserRepository userRepository;
    private final CustomerGroupRepository customerGroupRepository;

    // MODIFIED: The constructor now requires the UserRepository
    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, 
            CustomerGroupRepository customerGroupRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.customerGroupRepository = customerGroupRepository;
    }

    /**
     * ADDED: Helper method to securely get the Business of the currently authenticated user.
     * This is the core of the multi-tenancy security.
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

    // MODIFIED: This now finds all customers ONLY for the current user's business.
    public List<Customer> findAll() { 
        return customerRepository.findByBusiness(getCurrentBusiness()); 
    }

    // MODIFIED: This now finds a customer ONLY if they belong to the current user's business.
    // It now returns a Customer directly or throws an exception, which is a cleaner pattern for services.
    public Customer findById(Long id) { 
        return customerRepository.findByIdAndBusiness(id, getCurrentBusiness())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id));
    }

    // PRESERVED & SECURED: Your findByPartyCode method is now secure.
    public Optional<Customer> findByPartyCode(String partyCode) { 
        return customerRepository.findByPartyCodeAndBusiness(partyCode, getCurrentBusiness()); 
    }

    // MODIFIED: This now automatically assigns the business to new customers.
    @Transactional
    public Customer save(Customer customer) {
        Business currentBusiness = getCurrentBusiness();
        
        // Ensure the business is set, especially for new customers
        if (customer.getBusiness() == null) {
            customer.setBusiness(currentBusiness);
        }

        // Check if the customer is being created or updated without a group
        if (customer.getCustomerGroup() == null) {
            // Find the "Default" group or create it if it doesn't exist
            CustomerGroup defaultGroup = customerGroupRepository
                .findByNameAndBusiness("Default", currentBusiness)
                .orElseGet(() -> {
                    CustomerGroup newDefaultGroup = new CustomerGroup();
                    newDefaultGroup.setName("Default");
                    newDefaultGroup.setBusiness(currentBusiness);
                    return customerGroupRepository.save(newDefaultGroup);
                });
            
            // Assign the default group to the customer
            customer.setCustomerGroup(defaultGroup);
        }

        return customerRepository.save(customer);
    }


    // MODIFIED: This now deletes a customer ONLY if they belong to the current user's business.
    @Transactional
    public void deleteById(Long id) {
        Business currentBusiness = getCurrentBusiness();
        if (!customerRepository.existsByIdAndBusiness(id, currentBusiness)) {
            throw new EntityNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }
    
    // PRESERVED & SECURED: Your updateDue method is now automatically secure because it calls the secure findById method.
    @Transactional
    public void updateDue(Long customerId, BigDecimal delta) {
        // This call is now secure. If the customerId does not belong to the current
        // business, findById will throw an exception, and the code will stop.
        Customer customer = findById(customerId); 
        customer.setDue(customer.getDue().add(delta));
        customerRepository.save(customer);
    }
    
    
    public Customer update(Long id, Customer form) {
        // Securely find the existing customer. This throws an exception if not found.
        Customer existingCustomer = this.findById(id);

        // Update all fields from the form object.
        existingCustomer.setName(form.getName());
        existingCustomer.setCounterName(form.getCounterName());
        existingCustomer.setAddress(form.getAddress());
        existingCustomer.setHouseAddress(form.getHouseAddress());
        existingCustomer.setLandmark(form.getLandmark());
        existingCustomer.setPhone(form.getPhone());
        existingCustomer.setEmail(form.getEmail());
        existingCustomer.setGst(form.getGst());
        existingCustomer.setPartyCode(form.getPartyCode());
        existingCustomer.setCreditLimit(form.getCreditLimit());
        existingCustomer.setDue(form.getDue());
        existingCustomer.setCustomerGroup(form.getCustomerGroup());
        
        // The existing save method will handle the update in the database
        return this.save(existingCustomer);
    }
}

