package com.billingsolutions.service;

import com.billingsolutions.model.Product;
import com.billingsolutions.model.UnitType;
import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal sellingPrice;
    private UnitType unitType;
    private BigDecimal weightPerItem;
    private Integer unitsPerBox;
    private BigDecimal totalBagWeight;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.sellingPrice = product.getSellingPrice();
        this.unitType = product.getUnitType();
        this.weightPerItem = product.getWeightPerItem();
        this.unitsPerBox = product.getUnitsPerBox();
        this.totalBagWeight = product.getTotalBagWeight();
    }

    // Add all Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public UnitType getUnitType() { return unitType; }
    public BigDecimal getWeightPerItem() { return weightPerItem; }
    public Integer getUnitsPerBox() { return unitsPerBox; }
    public BigDecimal getTotalBagWeight() { return totalBagWeight; }
}