package com.billingsolutions.repository;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Finds a user by their unique username. Used for login and security checks.
	 */
	Optional<User> findByUsername(String username);
	
	/**
	 * NOTE: This method is insecure in a multi-tenant environment as it checks across all businesses.
	 * It is kept for legacy purposes but should be used with caution.
	 */
	boolean existsByRolesContaining(String role);

    // --- NEW METHODS FOR MULTI-TENANCY ---

    /**
     * Finds all users that belong to a specific business.
     */
    List<User> findByBusiness(Business business);

    /**
     * Securely finds a single user by their ID, but only if they belong to the specified business.
     */
    Optional<User> findByIdAndBusiness(Long id, Business business);

    /**
     * Finds all users with a specific role within a specific business.
     * This replaces the old, insecure findByRolesContaining method for salesmen lookups.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.business = :business AND r = :role")
    List<User> findByBusinessAndRolesContaining(@Param("business") Business business, @Param("role") String role);
}

