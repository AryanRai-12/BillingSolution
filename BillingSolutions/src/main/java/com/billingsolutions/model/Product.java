package com.billingsolutions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column
    private String hsn;
    
    
    @Column(unique = true)
    private String sku;
    
    @NotNull
    @DecimalMin("0.00")
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType unitType;

    // PCS-specific fields
    @Min(0)
    private Integer numberOfBoxes = 0;

    @Min(0)
    private Integer unitsPerBox = 0;

    @Min(0)
    private Integer pcs = 0; // loose pieces

    // KG-specific fields
    @DecimalMin("0.00")
    private BigDecimal weightPerItem; // weight of each item in kg

    @DecimalMin("0.00")
    private BigDecimal totalBagWeight; // weight of each bag in kg

    @Min(0)
    private Integer totalBags = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal gstRate = BigDecimal.ZERO;
    
    
    private String ProductGroup;

    public Product() {}

    // ===== Derived Helper Methods =====
    public Integer getStockPieces() {
        if (unitType == UnitType.PCS) {
            return (numberOfBoxes * unitsPerBox) + pcs;
        }
        return null; // not applicable for KG
    }

    public BigDecimal getTotalWeight() {
        if (unitType == UnitType.KG && totalBagWeight != null && totalBags != null) {
            return totalBagWeight.multiply(BigDecimal.valueOf(totalBags));
        }
        return null; // not applicable for PCS
    }

    // ===== Getters and Setters =====
    
    
    public Long getId() {
        return id;
    }

    public Business getBusiness() {
		return business;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public Integer getNumberOfBoxes() {
        return numberOfBoxes;
    }

    public void setNumberOfBoxes(Integer numberOfBoxes) {
        this.numberOfBoxes = numberOfBoxes;
    }

    public Integer getUnitsPerBox() {
        return unitsPerBox;
    }

    public void setUnitsPerBox(Integer unitsPerBox) {
        this.unitsPerBox = unitsPerBox;
    }

    public Integer getPcs() {
        return pcs;
    }

    public void setPcs(Integer pcs) {
        this.pcs = pcs;
    }

    public BigDecimal getWeightPerItem() {
        return weightPerItem;
    }

    public void setWeightPerItem(BigDecimal weightPerItem) {
        this.weightPerItem = weightPerItem;
    }

    public BigDecimal getTotalBagWeight() {
        return totalBagWeight;
    }

    public void setTotalBagWeight(BigDecimal totalBagWeight) {
        this.totalBagWeight = totalBagWeight;
    }

    public Integer getTotalBags() {
        return totalBags;
    }

    public void setTotalBags(Integer totalBags) {
        this.totalBags = totalBags;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public BigDecimal getGstRate() {
        return gstRate;
    }

    public void setGstRate(BigDecimal gstRate) {
        this.gstRate = gstRate;
    }

	public String getProductGroup() {
		return ProductGroup;
	}

	public void setProductGroup(String group) {
		ProductGroup = group;
	}

	public String getHsn() {
	    return hsn;
	}

	public void setHsn(String hsn) {
	    this.hsn = hsn;
	}
    
	
}
