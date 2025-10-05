package com.billingsolutions.repository;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.vendor")
    List<Product> findAllWithVendors();
    
 // --- METHODS FROM YOUR OLD REPOSITORY (NOW UPDATED FOR MULTI-TENANCY) ---

    /**
     * UPDATED: Finds a product by its SKU, but only within a specific business.
     * This prevents SKU conflicts between different businesses.
     */
    Optional<Product> findBySkuAndBusiness(String sku, Business business);

    /**
     * REPLACED: The old `findAllWithVendors` is replaced by this new, secure version.
     * Finds all products for a business, eagerly fetching vendor info.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.vendor WHERE p.business = :business")
    List<Product> findAllWithVendorsByBusiness(@Param("business") Business business);


    // --- NEW METHODS REQUIRED FOR MULTI-TENANCY ---

    /**
     * NEW: Finds all products that belong to a specific business.
     * This will be the primary method for listing products.
     */
    List<Product> findByBusiness(Business business);

    /**
     * NEW: Securely finds a single product by its ID and business.
     * This prevents users from one business from accessing another's data.
     */
    Optional<Product> findByIdAndBusiness(Long id, Business business);

    /**
     * NEW: Securely deletes a product.
     * Ensures a user can only delete products belonging to their own business.
     */
    boolean existsByIdAndBusiness(Long id, Business business);

    void deleteByIdAndBusiness(Long id, Business business);
}
