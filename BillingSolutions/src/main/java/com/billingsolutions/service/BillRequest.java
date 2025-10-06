package com.billingsolutions.service;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.BillItem;
import com.billingsolutions.model.UnitType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO used for creating a new Bill.
 */
public class BillRequest {
	private Long id; 
    private Long customerId;
    private Long salesmanId;
    private String customerName; 
    private String salesmanName; 
    private BigDecimal paymentAgainstPreviousDue;
    private List<PaymentRequest> payments = new ArrayList<>();
    private List<ItemRequest> items = new ArrayList<>();
    private String financialYear;
    
    public BillRequest() {}
    
    public BillRequest(Bill bill) {
        this.id = bill.getId();
        this.customerId = bill.getCustomer().getId();
        this.customerName = bill.getCustomer().getName(); 
        if (bill.getSalesman() != null) {
            this.salesmanId = bill.getSalesman().getId();
            this.salesmanName = bill.getSalesman().getUsername(); // ADD THIS
        }
        this.paymentAgainstPreviousDue = bill.getPaymentAgainstPreviousDue();
        this.financialYear = bill.getFinancialYear();
        if (bill.getItems() != null) {
            this.items = bill.getItems().stream()
                .map(ItemRequest::new) // This now works because the constructor below exists
                .collect(Collectors.toList());
        }
    }
    
    public String getCustomerName() { 
        return customerName; 
    }
    public void setCustomerName(String customerName) { 
        this.customerName = customerName; 
    }
    
    public String getSalesmanName() { 
        return salesmanName; 
    }
    public void setSalesmanName(String salesmanName) { 
        this.salesmanName = salesmanName; 
    }
    
    public List<PaymentRequest> getPayments() { 
        return payments; 
    }
    public void setPayments(List<PaymentRequest> payments) { 
        this.payments = payments; 
    }
    
    // ADDED: Getter and setter for the new financialYear field.
    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public BigDecimal getPaymentAgainstPreviousDue() {
        return paymentAgainstPreviousDue;
    }

    public void setPaymentAgainstPreviousDue(BigDecimal paymentAgainstPreviousDue) {
        this.paymentAgainstPreviousDue = paymentAgainstPreviousDue;
    }

    public List<ItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemRequest> items) {
        this.items = items;
    }

    public static class ItemRequest {
    	private Long id;
        private Long productId;
        private String productName;
        private UnitType unitType;
        private BigDecimal unitPrice; // ADDED: The missing field
        private int quantity;
        private BigDecimal discountPercent = BigDecimal.ZERO;
        
        public ItemRequest() {}
        
        public ItemRequest(BillItem item) {
        	this.id = item.getId();
            this.productId = item.getProduct().getId();
            this.productName = item.getProduct().getName();
            this.unitType = item.getUnitType();
            this.quantity = item.getQuantity();
            this.discountPercent = item.getDiscountPercent();
        }
        
        
        public String getProductName() { return productName; } // ADDED
        public void setProductName(String productName) { this.productName = productName; } // ADDED

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public UnitType getUnitType() {
            return unitType;
        }

        public void setUnitType(UnitType unitType) {
            this.unitType = unitType;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getDiscountPercent() {
            return discountPercent;
        }

        public void setDiscountPercent(BigDecimal discountPercent) {
            this.discountPercent = discountPercent;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}


