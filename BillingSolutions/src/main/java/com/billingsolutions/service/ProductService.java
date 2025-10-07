package com.billingsolutions.service;

import com.billingsolutions.model.Business;
import com.billingsolutions.model.Product;
import com.billingsolutions.model.UnitType;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.ProductRepository;
import com.billingsolutions.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
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

    /**
     * Saves a product, including business logic for PCS calculation and multi-tenant security.
     */
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setBusiness(getCurrentBusiness());
        }

        // --- RE-INTRODUCED BUSINESS LOGIC ---
        if (product.getUnitType() == UnitType.PCS) {
            int unitsPerBox = product.getUnitsPerBox() != null ? product.getUnitsPerBox() : 0;
            int numberOfBoxes = product.getNumberOfBoxes() != null ? product.getNumberOfBoxes() : 0;
            product.setPcs(unitsPerBox * numberOfBoxes);
            product.setTotalBags(0);
            product.setWeightPerItem(BigDecimal.ZERO);
            product.setTotalBagWeight(BigDecimal.ZERO);
        } else if (product.getUnitType() == UnitType.KG) {
            if (product.getWeightPerItem() != null && product.getWeightPerItem().compareTo(BigDecimal.ZERO) > 0 &&
                product.getTotalBagWeight() != null && product.getTotalBags() != null) {
                BigDecimal itemsPerBag = product.getTotalBagWeight().divide(product.getWeightPerItem(), 0, RoundingMode.FLOOR);
                product.setPcs(itemsPerBag.intValue() * product.getTotalBags());
            } else {
                product.setPcs(0);
            }
            product.setUnitsPerBox(0);
            product.setNumberOfBoxes(0);
        }
        // --- END OF BUSINESS LOGIC ---

        return productRepository.save(product);
    }

    /**
     * Securely finds all products ONLY for the current user's business.
     */
    public List<Product> findAll() {
        return productRepository.findByBusiness(getCurrentBusiness());
    }

    /**
     * Securely finds a product ONLY if it belongs to the current user's business.
     */
    public Product findById(Long id) {
        return productRepository.findByIdAndBusiness(id, getCurrentBusiness())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id + " for this business."));
    }

    /**
     * Securely deletes a product ONLY if it belongs to the current user's business.
     */
    public void deleteById(Long id) {
        if (!productRepository.existsByIdAndBusiness(id, getCurrentBusiness())) {
             throw new EntityNotFoundException("Product not found with ID: " + id + " for this business.");
        }
        productRepository.deleteById(id);
    }
    
    /**
     * Securely gets all products with their vendors for the billing page.
     */
    public List<Product> findAllWithVendorsForCurrentBusiness() {
        return productRepository.findAllWithVendorsByBusiness(getCurrentBusiness());
    }

    // ===== Stock Adjustments (Now Automatically Secure) =====

    public void adjustStockPCS(Long productId, int deltaBoxes, int deltaPcs) {
        Product product = findById(productId); // This call is now secure.

        if (product.getUnitType() != UnitType.PCS) {
            throw new IllegalStateException("This operation is only valid for PCS products.");
        }

        int newBoxes = (product.getNumberOfBoxes() == null ? 0 : product.getNumberOfBoxes()) + deltaBoxes;
        int newPcs = (product.getPcs() == null ? 0 : product.getPcs()) + deltaPcs;

        if (newBoxes < 0 || newPcs < 0) {
            throw new IllegalArgumentException("Stock cannot go negative.");
        }

        product.setNumberOfBoxes(newBoxes);
        product.setPcs(newPcs);

        save(product);
    }

    public void adjustStockByPieces(Long productId, int deltaPieces) {
        Product product = findById(productId); // This call is now secure.

        if (product.getUnitType() != UnitType.PCS) {
            throw new IllegalStateException("This operation is only valid for PCS products.");
        }

        if (product.getUnitsPerBox() == null || product.getUnitsPerBox() <= 0) {
            throw new IllegalArgumentException("Units per box not defined.");
        }
        
        // Note: product.getPcs() from the database already represents the total stock in pieces.
        int currentTotalPieces = product.getPcs(); 
        int newTotalPieces = currentTotalPieces + deltaPieces;

        if (newTotalPieces < 0) {
            throw new IllegalArgumentException("Stock cannot go negative (pieces).");
        }

        int boxes = newTotalPieces / product.getUnitsPerBox();
        int loosePcs = newTotalPieces % product.getUnitsPerBox();

        product.setNumberOfBoxes(boxes);
        // This seems to be an issue in the original logic. `setPcs` should store the TOTAL pieces.
        // If it's meant to be loose pieces, then the calculation is different. Assuming `pcs` is total stock.
        product.setPcs(newTotalPieces); 

        save(product);
    }

    public void adjustStockKG(Long productId, int deltaBags, BigDecimal weightPerItem, BigDecimal totalBagWeight) {
        Product product = findById(productId); // This call is now secure.

        if (product.getUnitType() != UnitType.KG) {
            throw new IllegalStateException("This operation is only valid for KG products.");
        }

        int currentBags = product.getTotalBags() == null ? 0 : product.getTotalBags();
        int newBags = currentBags + deltaBags;

        if (newBags < 0) {
            throw new IllegalArgumentException("Stock cannot go negative (bags).");
        }

        product.setTotalBags(newBags);
        product.setWeightPerItem(weightPerItem);
        product.setTotalBagWeight(totalBagWeight);

        save(product);
    }

    public void adjustStockBoxes(Long productId, int boxesChange) {
        Product product = findById(productId); // This call is now secure.

        int currentBoxes = product.getNumberOfBoxes() != null ? product.getNumberOfBoxes() : 0;
        int updatedBoxes = currentBoxes + boxesChange;

        if (updatedBoxes < 0) {
            throw new IllegalArgumentException("Not enough boxes in stock");
        }

        product.setNumberOfBoxes(updatedBoxes);
        save(product);
    }

    public void adjustStockPcs(Long productId, int pcsChange) {
        Product product = findById(productId); // This call is now secure.

        int currentPcs = product.getPcs() != null ? product.getPcs() : 0;
        int updatedPcs = currentPcs + pcsChange;

        if (updatedPcs < 0) {
            throw new IllegalArgumentException("Not enough pieces in stock");
        }

        product.setPcs(updatedPcs);
        save(product);
    }

    public void adjustStockBags(Long productId, int bagsChange) {
        Product product = findById(productId); // This call is now secure.

        int currentBags = product.getTotalBags() != null ? product.getTotalBags() : 0;
        int updatedBags = currentBags + bagsChange;

        if (updatedBags < 0) {
            throw new IllegalArgumentException("Not enough bags in stock");
        }

        product.setTotalBags(updatedBags);
        save(product);
    }
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySkuAndBusiness(sku, getCurrentBusiness());
    }

    /**
     * Securely checks if a product with the given SKU exists, but only within the current user's business.
     */
    public boolean existsBySku(String sku) {
        return productRepository.existsBySkuAndBusiness(sku, getCurrentBusiness());
    }
}

