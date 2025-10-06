package com.billingsolutions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "bill_items")
public class BillItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@JsonBackReference
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "bill_id")
	private Bill bill;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UnitType unitType = UnitType.PCS;

	@NotNull
	@Column(nullable = false)
	private Integer quantity = 1;

	@NotNull
	@Column(nullable = false)
	private Integer boxesCount = 0;

	@NotNull
	@Column(nullable = false)
	private Integer pcsPerBox = 1;

	@NotNull
	@Column(precision = 5, scale = 2)
	private BigDecimal discountPercent = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal unitPriceSnapshot = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal unitCostSnapshot = BigDecimal.ZERO;

	@NotNull
	@Column(nullable = false)
	private Integer totalPieces = 0;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal lineDiscount = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal lineTotal = BigDecimal.ZERO;
	
	// ADDED: Snapshot of the GST Rate at the time of billing
	@NotNull
	@Column(precision = 5, scale = 2)
	private BigDecimal gstRateSnapshot = BigDecimal.ZERO;

	// ADDED: Calculated tax amount for this line item
	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal lineTax = BigDecimal.ZERO;


	public BillItem() {}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Bill getBill() { return bill; }
	public void setBill(Bill bill) { this.bill = bill; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
	public UnitType getUnitType() { return unitType; }
	public void setUnitType(UnitType unitType) { this.unitType = unitType; }
	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
	public Integer getBoxesCount() { return boxesCount; }
	public void setBoxesCount(Integer boxesCount) { this.boxesCount = boxesCount; }
	public Integer getPcsPerBox() { return pcsPerBox; }
	public void setPcsPerBox(Integer pcsPerBox) { this.pcsPerBox = pcsPerBox; }
	public BigDecimal getDiscountPercent() { return discountPercent; }
	public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
	public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
	public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) { this.unitPriceSnapshot = unitPriceSnapshot; }
	public BigDecimal getUnitCostSnapshot() { return unitCostSnapshot; }
	public void setUnitCostSnapshot(BigDecimal unitCostSnapshot) { this.unitCostSnapshot = unitCostSnapshot; }
	public Integer getTotalPieces() { return totalPieces; }
	public void setTotalPieces(Integer totalPieces) { this.totalPieces = totalPieces; }
	public BigDecimal getLineDiscount() { return lineDiscount; }
	public void setLineDiscount(BigDecimal lineDiscount) { this.lineDiscount = lineDiscount; }
	public BigDecimal getLineTotal() { return lineTotal; }
	public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
	public BigDecimal getGstRateSnapshot() { return gstRateSnapshot; }
	public void setGstRateSnapshot(BigDecimal gstRateSnapshot) { this.gstRateSnapshot = gstRateSnapshot; }
	public BigDecimal getLineTax() { return lineTax; }
	public void setLineTax(BigDecimal lineTax) { this.lineTax = lineTax; }
} 