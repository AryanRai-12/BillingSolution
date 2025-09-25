package com.billingsolutions.repository;

import com.billingsolutions.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.vendor")
    List<Product> findAllWithVendors();
}
