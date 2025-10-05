package com.billingsolutions.repository;

import com.billingsolutions.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    /**
     * Checks if a business with the given email already exists.
     * This is crucial for ensuring each business has a unique owner email.
     * @param email The owner's email to check.
     * @return true if a business with this email exists, false otherwise.
     */
    boolean existsByOwnerEmail(String email);

    /**
     * Finds a business by its owner's email address.
     * @param email The owner's email to search for.
     * @return An Optional containing the Business if found.
     */
    Optional<Business> findByOwnerEmail(String email);
}