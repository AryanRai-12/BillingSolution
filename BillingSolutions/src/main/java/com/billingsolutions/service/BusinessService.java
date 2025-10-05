package com.billingsolutions.service;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.BusinessRepository;
import com.billingsolutions.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BusinessService(BusinessRepository businessRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new business and its first administrator user.
     * This method is transactional, meaning both the business and the user must be
     * created successfully, or the entire operation will be rolled back.
     *
     * @param request The DTO containing registration details.
     * @return The newly created Business entity.
     * @throws IllegalStateException if a business with the given email already exists.
     */
    @Transactional
    public Business registerNewBusiness(BusinessRegistrationRequest request) {
        // Step 1: Validate that the owner's email is not already in use.
        if (businessRepository.existsByOwnerEmail(request.getOwnerEmail())) {
            throw new IllegalStateException("A business with the email " + request.getOwnerEmail() + " already exists.");
        }

        // Step 2: Create and save the new Business entity.
        Business newBusiness = new Business();
        newBusiness.setName(request.getBusinessName());
        newBusiness.setGstNumber(request.getGstNumber());
        newBusiness.setOwnerEmail(request.getOwnerEmail());
        businessRepository.save(newBusiness);

        // Step 3: Create the initial administrator user for this business.
        User adminUser = new User();
        adminUser.setUsername(request.getAdminUsername());
        adminUser.setEmail(request.getOwnerEmail()); // Often the owner's email is used for the primary admin
        adminUser.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        adminUser.setRoles(Set.of("ROLE_ADMIN"));
        adminUser.setEnabled(true);
        
        // Step 4: CRUCIAL - Link the new user to the new business.
        adminUser.setBusiness(newBusiness);
        
        userRepository.save(adminUser);

        return newBusiness;
    }
}
