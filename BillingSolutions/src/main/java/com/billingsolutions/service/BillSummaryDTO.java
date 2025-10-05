package com.billingsolutions.service;



import com.billingsolutions.model.Bill;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A simple Data Transfer Object (DTO) to securely send bill summary
 * information to the frontend without causing serialization issues.
 */
public class BillSummaryDTO {

    private Long id;
    private String billNo;
    private String customerNameSnapshot;
    private LocalDateTime createdAt;
    private BigDecimal total;
    private String financialYear;
    private final boolean editable;

    // This constructor makes it easy to convert a Bill entity to a DTO
    public BillSummaryDTO(Bill bill,boolean isEditable) {
        this.id = bill.getId();
        this.billNo = bill.getBillNo();
        this.customerNameSnapshot = bill.getCustomerNameSnapshot();
        this.createdAt = bill.getCreatedAt();
        this.total = bill.getTotal();
        this.financialYear = bill.getFinancialYear();
        this.editable = isEditable;
    }

    // Standard Getters
    public Long getId() { return id; }
    public String getBillNo() { return billNo; }
    public String getCustomerNameSnapshot() { return customerNameSnapshot; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getTotal() { return total; }
    public String getFinancialYear() { return financialYear; }
    public boolean isEditable() { return editable; }
}

