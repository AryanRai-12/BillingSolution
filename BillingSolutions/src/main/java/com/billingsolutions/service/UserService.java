package com.billingsolutions.service;

import com.billingsolutions.model.User;
import com.billingsolutions.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks if an admin account already exists in the database.
     * This is used by the controller to decide if the registration page should be shown.
     * @return true if an admin exists, false otherwise.
     */
    public boolean hasAdminAccount() {
        // We check for "ROLE_ADMIN" because that's the standard prefix Spring Security uses.
        return userRepository.existsByRolesContaining("ROLE_ADMIN");
    }

    /**
     * Registers the first user as an ADMIN after successful OTP verification.
     * This method hashes the password, sets the role, and saves the new admin.
     */
    @Transactional
    public void registerAdmin(User user) {
        if (hasAdminAccount()) {
            // This is a safety check to prevent creating more than one admin.
            throw new IllegalStateException("An admin account already exists. Cannot create another.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of("ROLE_ADMIN"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Creates a new standard user with the "ROLE_USER" role.
     * This method is intended to be called by an existing admin from a user management dashboard.
     */
    @Transactional
    public void createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of("ROLE_USER"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    // --- Your existing utility methods ---

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User save(User user) {
        // Note: This is a general save method. For specific user creation,
        // use registerAdmin() or createUser() to ensure roles are set correctly.
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
} 