package com.billingsolutions.service;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // MODIFIED: The PasswordEncoder is no longer needed here as it's handled by the controllers.
    public UserService(UserRepository userRepository) {
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
     * SECURE: Finds all users, but only for the current admin's business.
     */
    public List<User> findAll() {
        return userRepository.findByBusiness(getCurrentBusiness());
    }

    /**
     * SECURE: Finds a single user by ID, but only if they belong to the current admin's business.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findByIdAndBusiness(id, getCurrentBusiness());
    }

    /**
     * SECURE: Saves a user, automatically assigning them to the current admin's business if they are new.
     */
    public User save(User user) {
        // If this is a new user (no ID yet), securely associate them with the current business.
        if (user.getId() == null) {
            user.setBusiness(getCurrentBusiness());
        }
        return userRepository.save(user);
    }

    /**
     * SECURE: Deletes a user, but only if they belong to the current admin's business.
     */
    public void deleteById(Long id) {
        // First, securely find the user to ensure they belong to this business before deleting.
        User userToDelete = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userRepository.delete(userToDelete);
    }

    // This method is still needed for Spring Security to find users by username during login.
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // REMOVED: The hasAdminAccount() and registerAdmin() methods are now part of the BusinessService logic.
    // REMOVED: The old createUser() method is replaced by the secure save() and handled in the UserController.
}

