package com.billingsolutions.service;

import com.billingsolutions.model.UnitType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO used for creating a new Bill.
 */
public class BillRequest {
    private Long customerId;
    private Long salesmanId;
    private BigDecimal paymentAgainstPreviousDue;
    private List<ItemRequest> items = new ArrayList<>();

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
        private Long productId;
        private UnitType unitType;
        private int quantity;
        private BigDecimal discountPercent = BigDecimal.ZERO;

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
    }
}


