package com.billingsolutions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "bills", uniqueConstraints = {
	    // MODIFIED: The unique key now includes business_id.
	    @UniqueConstraint(columnNames = {"billNo", "financialYear", "business_id"})
	})
public class Bill {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonIgnore
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "business_id")
	private Business business;
	
	@Column(nullable = false, updatable = false)
	private String financialYear;

	@JsonIgnore
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
	
	@JsonManagedReference
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
	
//	@NotNull
//	@ManyToOne
//	@JoinColumn(name = "salesman_id")
//	private Salesman salesman;
	
	@Column(nullable = false, updatable = false)
	private String createdBy;
	
	@Column(nullable = false)
    private String billNo;
	
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "salesman_user_id") // A new column in the bills table
	private User salesman;
	
	
	// ADDED: A relationship to track all payments made against this specific bill.
    @JsonManagedReference("bill-payments")
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();
    
    @NotNull
	@Column(precision = 18, scale = 2)
	private BigDecimal totalTax = BigDecimal.ZERO;

    
    
    /**
     * THIS IS THE FIX: A new helper method to perform the complex calculation.
     * It sums all the payments associated with this bill.
     * @return The total amount paid against this bill.
     */
    @Transient // This tells Hibernate not to try and save this as a database column
    public BigDecimal getAmountPaid() {
        if (this.payments == null) {
            return BigDecimal.ZERO;
        }
        return this.payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * THIS IS THE FIX: A second helper method for the remaining due.
     * It uses the getAmountPaid() method for a clean calculation.
     * @return The remaining due amount for this specific bill.
     */
    @Transient
    public BigDecimal getRemainingDue() {
        return this.getTotal().subtract(getAmountPaid());
    }
    
    
    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
	
	public String getFinancialYear() { 
		return financialYear; 
	}
	public void setFinancialYear(String financialYear) { 
		this.financialYear = financialYear; 
	}
	
	public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }
	
	public User getSalesman() {
		return salesman;
	}

	public void setSalesman(User salesman) {
		this.salesman = salesman;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Bill() {}

	public void addItem(BillItem item) {
		item.setBill(this);
		this.items.add(item);
	}
	
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	
	
	public Business getBusiness() {
		return business;
	}
	public void setBusiness(Business business) {
		this.business = business;
	}
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
	public BigDecimal getTotalTax() { return totalTax; }
	public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }
}
