package com.billingsolutions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
public class Bill {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@Column(nullable = false)
	private String customerNameSnapshot;

	@Column
	private String customerPhoneSnapshot;

	@Column
	private String customerAddressSnapshot;

	@Column(length = 15)
	private String customerGstSnapshot;

	@Column(nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<BillItem> items = new ArrayList<>();

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal subTotal = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal totalDiscount = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal previousDue = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal paymentAgainstPreviousDue = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal newDue = BigDecimal.ZERO;

	@NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal totalCostBasis = BigDecimal.ZERO;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "salesman_id")
	private Salesman salesman;

	public Salesman getSalesman() {
		return salesman;
	}

	public void setSalesman(Salesman salesman) {
		this.salesman = salesman;
	}

	public Bill() {}

	public void addItem(BillItem item) {
		item.setBill(this);
		this.items.add(item);
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }
	public String getCustomerNameSnapshot() { return customerNameSnapshot; }
	public void setCustomerNameSnapshot(String customerNameSnapshot) { this.customerNameSnapshot = customerNameSnapshot; }
	public String getCustomerPhoneSnapshot() { return customerPhoneSnapshot; }
	public void setCustomerPhoneSnapshot(String customerPhoneSnapshot) { this.customerPhoneSnapshot = customerPhoneSnapshot; }
	public String getCustomerAddressSnapshot() { return customerAddressSnapshot; }
	public void setCustomerAddressSnapshot(String customerAddressSnapshot) { this.customerAddressSnapshot = customerAddressSnapshot; }
	public String getCustomerGstSnapshot() { return customerGstSnapshot; }
	public void setCustomerGstSnapshot(String customerGstSnapshot) { this.customerGstSnapshot = customerGstSnapshot; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public List<BillItem> getItems() { return items; }
	public void setItems(List<BillItem> items) { this.items = items; }
	public BigDecimal getSubTotal() { return subTotal; }
	public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
	public BigDecimal getTotalDiscount() { return totalDiscount; }
	public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }
	public BigDecimal getTotal() { return total; }
	public void setTotal(BigDecimal total) { this.total = total; }
	public BigDecimal getPreviousDue() { return previousDue; }
	public void setPreviousDue(BigDecimal previousDue) { this.previousDue = previousDue; }
	public BigDecimal getPaymentAgainstPreviousDue() { return paymentAgainstPreviousDue; }
	public void setPaymentAgainstPreviousDue(BigDecimal paymentAgainstPreviousDue) { this.paymentAgainstPreviousDue = paymentAgainstPreviousDue; }
	public BigDecimal getNewDue() { return newDue; }
	public void setNewDue(BigDecimal newDue) { this.newDue = newDue; }
	public BigDecimal getTotalCostBasis() { return totalCostBasis; }
	public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }
} 