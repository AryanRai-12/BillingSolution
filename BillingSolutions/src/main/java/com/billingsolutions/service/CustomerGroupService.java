package com.billingsolutions.service;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.CustomerGroup;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.CustomerGroupRepository;
import com.billingsolutions.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerGroupService {

    private final CustomerGroupRepository customerGroupRepository;
    private final UserRepository userRepository;

    @Autowired
    public CustomerGroupService(CustomerGroupRepository customerGroupRepository, UserRepository userRepository) {
        this.customerGroupRepository = customerGroupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Helper method to securely get the Business of the currently authenticated user.
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

    /**
     * Finds a customer group by its ID, ensuring it belongs to the current business.
     * @param id The ID of the customer group.
     * @return The found CustomerGroup entity.
     * @throws EntityNotFoundException if the group doesn't exist or doesn't belong to the current business.
     */
    public CustomerGroup findById(Long id) {
        return customerGroupRepository.findByIdAndBusiness(id, getCurrentBusiness())
                .orElseThrow(() -> new EntityNotFoundException("CustomerGroup not found with ID: " + id));
    }
    
    /**
     * Returns a list of all customer groups for the currently logged-in user's business.
     * @return A list of CustomerGroup entities.
     */
    public List<CustomerGroup> findAllForCurrentBusiness() {
        return customerGroupRepository.findByBusinessOrderByNameAsc(getCurrentBusiness());
    }

    /**
     * Saves a new or existing CustomerGroup. For new groups, it automatically
     * associates it with the current business.
     * @param customerGroup The entity to save.
     * @return The saved entity.
     */
    public CustomerGroup save(CustomerGroup customerGroup) {
        if (customerGroup.getId() == null) {
            customerGroup.setBusiness(getCurrentBusiness());
        }
        return customerGroupRepository.save(customerGroup);
    }
    
    /**
     * Deletes a customer group by its ID after verifying it belongs to the current business.
     * @param id The ID of the group to delete.
     * @throws EntityNotFoundException if the group is not found for the current business.
     */
    public void deleteById(Long id) {
        Business currentBusiness = getCurrentBusiness();
        if (!customerGroupRepository.existsByIdAndBusiness(id, currentBusiness)) {
            throw new EntityNotFoundException("CustomerGroup not found with ID: " + id);
        }
        customerGroupRepository.deleteById(id);
    }
    
    public CustomerGroup update(Long id, CustomerGroup updatedGroupData) {
        // This call securely finds the group, ensuring it belongs to the current business.
        CustomerGroup existingGroup = findById(id);
        
        // Update the name from the provided data
        existingGroup.setName(updatedGroupData.getName());
        
        // Save the updated entity
        return customerGroupRepository.save(existingGroup);
    }
}