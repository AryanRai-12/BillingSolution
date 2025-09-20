package com.billingsolutions.service;

import com.billingsolutions.model.Product;
import com.billingsolutions.model.UnitType;
import com.billingsolutions.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Save or update product
    public Product save(Product product) {
        return productRepository.save(product);
    }

    // Get all products
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // Get product by id
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    // Delete product
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
    

    // ===== Stock Adjustments =====

    @Transactional
    public void adjustStockPCS(Long productId, int deltaBoxes, int deltaPcs) {
        Product product = findById(productId);

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

    @Transactional
    public void adjustStockByPieces(Long productId, int deltaPieces) {
        Product product = findById(productId);

        if (product.getUnitType() != UnitType.PCS) {
            throw new IllegalStateException("This operation is only valid for PCS products.");
        }

        if (product.getUnitsPerBox() == null || product.getUnitsPerBox() <= 0) {
            throw new IllegalArgumentException("Units per box not defined.");
        }

        int currentTotalPieces = product.getStockPieces();
        int newTotalPieces = currentTotalPieces + deltaPieces;

        if (newTotalPieces < 0) {
            throw new IllegalArgumentException("Stock cannot go negative (pieces).");
        }

        int boxes = newTotalPieces / product.getUnitsPerBox();
        int pcs = newTotalPieces % product.getUnitsPerBox();

        product.setNumberOfBoxes(boxes);
        product.setPcs(pcs);

        save(product);
    }

    @Transactional
    public void adjustStockKG(Long productId, int deltaBags, BigDecimal weightPerItem, BigDecimal totalBagWeight) {
        Product product = findById(productId);

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

    @Transactional
    public void adjustStockBoxes(Long productId, int boxesChange) {
        Product product = findById(productId);

        int currentBoxes = product.getNumberOfBoxes() != null ? product.getNumberOfBoxes() : 0;
        int updatedBoxes = currentBoxes + boxesChange;

        if (updatedBoxes < 0) {
            throw new IllegalArgumentException("Not enough boxes in stock");
        }

        product.setNumberOfBoxes(updatedBoxes);
        save(product);
    }

    @Transactional
    public void adjustStockPcs(Long productId, int pcsChange) {
        Product product = findById(productId);

        int currentPcs = product.getPcs() != null ? product.getPcs() : 0;
        int updatedPcs = currentPcs + pcsChange;

        if (updatedPcs < 0) {
            throw new IllegalArgumentException("Not enough pieces in stock");
        }

        product.setPcs(updatedPcs);
        save(product);
    }

    @Transactional
    public void adjustStockBags(Long productId, int bagsChange) {
        Product product = findById(productId);

        int currentBags = product.getTotalBags() != null ? product.getTotalBags() : 0;
        int updatedBags = currentBags + bagsChange;

        if (updatedBags < 0) {
            throw new IllegalArgumentException("Not enough bags in stock");
        }

        product.setTotalBags(updatedBags);
        save(product);
    }
}
