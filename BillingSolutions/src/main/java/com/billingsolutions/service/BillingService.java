package com.billingsolutions.service;

import com.billingsolutions.model.*;
import com.billingsolutions.model.UnitType;
import com.billingsolutions.repository.*;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class BillingService {
	private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalesmanRepository salesmanRepository;

    public BillingService(BillRepository billRepository,
                          ProductRepository productRepository,
                          CustomerRepository customerRepository,
                          SalesmanRepository salesmanRepository) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesmanRepository = salesmanRepository;
    }
    
    
    

    /**
     * Retrieves a single bill by its unique ID.
     * @param id The ID of the bill.
     * @return The found Bill entity.
     * @throws EntityNotFoundException if no bill with the given ID is found.
     */
    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + id));
    }

    /**
     * Creates a new bill after validating business rules like profitability.
     * This is a transactional method, ensuring all database operations succeed or fail together.
     * @param request The DTO containing all data from the billing form.
     * @return The newly created and saved Bill entity.
     * @throws InsufficientProfitException if any item is being sold at a loss.
     */
    @Transactional
    public Bill createBill(BillRequest request) {
        // STEP 1: Validate profitability for all items before any processing.
        for (BillRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + itemReq.getProductId()));

            BigDecimal sellingPrice = product.getSellingPrice();
            BigDecimal costPrice = product.getCostPrice();
            BigDecimal discountPercent = itemReq.getDiscountPercent() != null ? itemReq.getDiscountPercent() : BigDecimal.ZERO;

            BigDecimal discountAmount = sellingPrice.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal finalSellingPrice = sellingPrice.subtract(discountAmount);

            if (finalSellingPrice.compareTo(costPrice) < 0) {
                throw new InsufficientProfitException(
                    "Profit Alert: Product '" + product.getName() + "' is being sold at a loss. Bill creation denied."
                );
            }
        }
        
        // STEP 2: If validation passes, proceed with creating the bill.
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Customer ID: " + request.getCustomerId()));
        Salesman salesman = salesmanRepository.findById(request.getSalesmanId().intValue())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Salesman ID: " + request.getSalesmanId()));

        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setSalesman(salesman);

        // Snapshot customer information for historical accuracy
        bill.setCustomerNameSnapshot(customer.getName());
        bill.setCustomerPhoneSnapshot(customer.getPhone());
        bill.setCustomerAddressSnapshot(customer.getAddress());
        bill.setCustomerGstSnapshot(customer.getGst());

        List<BillItem> billItems = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        for (BillRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId()).get(); // We know it exists from the check above

            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProduct(product);
            item.setUnitType(itemReq.getUnitType());
            item.setQuantity(itemReq.getQuantity());
            item.setDiscountPercent(itemReq.getDiscountPercent());

            // Snapshot product prices at the time of sale
            item.setUnitPriceSnapshot(product.getSellingPrice());
            item.setUnitCostSnapshot(product.getCostPrice());
            
            int totalPieces = calculatePieces(product, itemReq.getUnitType(), itemReq.getQuantity());
            item.setTotalPieces(totalPieces);

            BigDecimal quantityBD = BigDecimal.valueOf(itemReq.getQuantity());
            BigDecimal grossLineTotal = product.getSellingPrice().multiply(quantityBD);
            BigDecimal discountAmount = grossLineTotal.multiply(itemReq.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);

            item.setLineDiscount(discountAmount);
            item.setLineTotal(netLineTotal);
            billItems.add(item);

            // Aggregate totals for the main bill
            subTotal = subTotal.add(grossLineTotal);
            totalDiscount = totalDiscount.add(discountAmount);
            totalCostBasis = totalCostBasis.add(product.getCostPrice().multiply(BigDecimal.valueOf(totalPieces)));

            // Reduce product stock from inventory
            reduceProductStock(product, itemReq.getUnitType(), itemReq.getQuantity(), totalPieces);
        }

        bill.setItems(billItems);
        bill.setSubTotal(subTotal);
        bill.setTotalDiscount(totalDiscount);
        bill.setTotal(subTotal.subtract(totalDiscount));
        bill.setTotalCostBasis(totalCostBasis);

        // STEP 3: Correctly handle customer dues
        BigDecimal previousDue = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;
        bill.setPreviousDue(previousDue);
        
        BigDecimal paymentAgainstDue = request.getPaymentAgainstPreviousDue() != null ? request.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
        bill.setPaymentAgainstPreviousDue(paymentAgainstDue);
        
        // New due = Previous Due - Payment + Current Bill Total
        BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
        bill.setNewDue(newDue);
        
        // Update the customer's balance
        customer.setDue(newDue);
        customerRepository.save(customer);

        return billRepository.save(bill);
    }
    
    /**
     * Helper method to calculate the total number of individual pieces sold.
     */
    private int calculatePieces(Product product, UnitType unitType, int quantity) {
        if (product.getUnitType() == UnitType.PCS) {
            switch (unitType) {
                case PCS: return quantity;
                case DOZEN: return quantity * 12;
                case HALF_DOZEN: return quantity * 6;
                case BOX: return quantity * (product.getUnitsPerBox() != null ? product.getUnitsPerBox() : 1);
                case KG: return quantity;
                case BAG: return quantity;
            }
        } else if (product.getUnitType() == UnitType.KG) {
            // Add logic for KG-based products if necessary
            return quantity; 
        }
        return quantity;
    }

    /**
     * Helper method to reduce product stock from inventory.
     */
    private void reduceProductStock(Product product, UnitType unitType, int quantity, int totalPieces) {
        if (product.getUnitType() == UnitType.PCS) {
            if (unitType == UnitType.BOX) {
                 product.setNumberOfBoxes(product.getNumberOfBoxes() - quantity);
            }
            // The total number of loose pieces is always the source of truth for stock.
            // (numberOfBoxes * unitsPerBox) + loosePcs
            Integer currentStock = product.getStockPieces();
            Integer remainingStock = currentStock - totalPieces;
            
            // Recalculate boxes and loose pieces
            if (product.getUnitsPerBox() != null && product.getUnitsPerBox() > 0) {
                product.setNumberOfBoxes(remainingStock / product.getUnitsPerBox());
                product.setPcs(remainingStock % product.getUnitsPerBox());
            } else {
                product.setNumberOfBoxes(0);
                product.setPcs(remainingStock);
            }

        } else if (product.getUnitType() == UnitType.KG) {
            // Logic for reducing KG/Bag stock
            if (unitType == UnitType.BAG) {
                product.setTotalBags(product.getTotalBags() - quantity);
            }
            // Add other KG stock reduction logic as needed
        }
        productRepository.save(product);
    }

    /**
     * Admin-side update of a bill: adjust per-item discounts and payment against previous due,
     * then recalculate bill totals and customer's due.
     */
    @Transactional
    public Bill adminUpdateBill(UpdateBillRequest request) {
        Bill bill = billRepository.findById(request.billId)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + request.billId));

        // Update payment against previous due if provided
        if (request.paymentAgainstPreviousDue != null) {
            bill.setPaymentAgainstPreviousDue(request.paymentAgainstPreviousDue);
        }

        // Map itemId -> new discount and apply
        if (request.items != null && !request.items.isEmpty()) {
            java.util.Map<Long, java.math.BigDecimal> itemIdToDiscount = new java.util.HashMap<>();
            for (UpdateBillItemRequest i : request.items) {
                if (i != null && i.itemId != null && i.discountPercent != null) {
                    itemIdToDiscount.put(i.itemId, i.discountPercent);
                }
            }

            for (BillItem item : bill.getItems()) {
                java.math.BigDecimal newDiscount = itemIdToDiscount.get(item.getId());
                if (newDiscount != null) {
                    item.setDiscountPercent(newDiscount);
                }

                // Recalculate line totals
                java.math.BigDecimal quantityBD = java.math.BigDecimal.valueOf(item.getQuantity());
                java.math.BigDecimal grossLineTotal = item.getUnitPriceSnapshot().multiply(quantityBD);
                java.math.BigDecimal discountAmount = grossLineTotal
                        .multiply(item.getDiscountPercent() == null ? java.math.BigDecimal.ZERO : item.getDiscountPercent())
                        .divide(new java.math.BigDecimal("100"), 2, RoundingMode.HALF_UP);
                java.math.BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);

                item.setLineDiscount(discountAmount);
                item.setLineTotal(netLineTotal);
            }
        }

        // Recalculate bill totals
        java.math.BigDecimal subTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalDiscount = java.math.BigDecimal.ZERO;
        for (BillItem item : bill.getItems()) {
            java.math.BigDecimal quantityBD = java.math.BigDecimal.valueOf(item.getQuantity());
            subTotal = subTotal.add(item.getUnitPriceSnapshot().multiply(quantityBD));
            totalDiscount = totalDiscount.add(item.getLineDiscount());
        }
        bill.setSubTotal(subTotal);
        bill.setTotalDiscount(totalDiscount);
        bill.setTotal(subTotal.subtract(totalDiscount));

        // Recompute new due and update customer due
        java.math.BigDecimal previousDue = bill.getPreviousDue() != null ? bill.getPreviousDue() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal paymentAgainstDue = bill.getPaymentAgainstPreviousDue() != null ? bill.getPaymentAgainstPreviousDue() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
        bill.setNewDue(newDue);

        Customer customer = bill.getCustomer();
        if (customer != null) {
            customer.setDue(newDue);
            customerRepository.save(customer);
        }

        return billRepository.save(bill);
    }

    // DTOs used by AdminController for updating bills
    public static class UpdateBillRequest {
        public Long billId;
        public java.math.BigDecimal paymentAgainstPreviousDue;
        public java.util.List<UpdateBillItemRequest> items;
    }

    public static class UpdateBillItemRequest {
        public Long itemId;
        public java.math.BigDecimal discountPercent;
    }
} 