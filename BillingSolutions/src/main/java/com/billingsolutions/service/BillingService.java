//package com.billingsolutions.service;
//
//import com.billingsolutions.model.*;
//import com.billingsolutions.model.UnitType;
//import com.billingsolutions.repository.*;
//
//import jakarta.persistence.EntityNotFoundException;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class BillingService {
//	private final BillRepository billRepository;
//    private final ProductRepository productRepository;
//    private final CustomerRepository customerRepository;
//    private final SalesmanRepository salesmanRepository;
//
//    public BillingService(BillRepository billRepository,
//                          ProductRepository productRepository,
//                          CustomerRepository customerRepository,
//                          SalesmanRepository salesmanRepository) {
//        this.billRepository = billRepository;
//        this.productRepository = productRepository;
//        this.customerRepository = customerRepository;
//        this.salesmanRepository = salesmanRepository;
//    }
//    
//    
//    
//
//    /**
//     * Retrieves a single bill by its unique ID.
//     * @param id The ID of the bill.
//     * @return The found Bill entity.
//     * @throws EntityNotFoundException if no bill with the given ID is found.
//     */
//    public Bill getBillById(Long id) {
//        return billRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + id));
//    }
//
//    /**
//     * Creates a new bill after validating business rules like profitability.
//     * This is a transactional method, ensuring all database operations succeed or fail together.
//     * @param request The DTO containing all data from the billing form.
//     * @return The newly created and saved Bill entity.
//     * @throws InsufficientProfitException if any item is being sold at a loss.
//     */
//    @Transactional
//    public Bill createBill(BillRequest request) {
//        // STEP 1: Validate profitability for all items before any processing.
//        for (BillRequest.ItemRequest itemReq : request.getItems()) {
//            Product product = productRepository.findById(itemReq.getProductId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + itemReq.getProductId()));
//
//            BigDecimal sellingPrice = product.getSellingPrice();
//            BigDecimal costPrice = product.getCostPrice();
//            BigDecimal discountPercent = itemReq.getDiscountPercent() != null ? itemReq.getDiscountPercent() : BigDecimal.ZERO;
//
//            BigDecimal discountAmount = sellingPrice.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//            BigDecimal finalSellingPrice = sellingPrice.subtract(discountAmount);
//
//            if (finalSellingPrice.compareTo(costPrice) < 0) {
//                throw new InsufficientProfitException(
//                    "Profit Alert: Product '" + product.getName() + "' is being sold at a loss. Bill creation denied."
//                );
//            }
//        }
//        
//        // STEP 2: If validation passes, proceed with creating the bill.
//        Customer customer = customerRepository.findById(request.getCustomerId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Customer ID: " + request.getCustomerId()));
//        Salesman salesman = salesmanRepository.findById(request.getSalesmanId().intValue())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Salesman ID: " + request.getSalesmanId()));
//
//        Bill bill = new Bill();
//        bill.setCustomer(customer);
//        bill.setSalesman(salesman);
//
//        // Snapshot customer information for historical accuracy
//        bill.setCustomerNameSnapshot(customer.getName());
//        bill.setCustomerPhoneSnapshot(customer.getPhone());
//        bill.setCustomerAddressSnapshot(customer.getAddress());
//        bill.setCustomerGstSnapshot(customer.getGst());
//
//        List<BillItem> billItems = new ArrayList<>();
//        BigDecimal subTotal = BigDecimal.ZERO;
//        BigDecimal totalDiscount = BigDecimal.ZERO;
//        BigDecimal totalCostBasis = BigDecimal.ZERO;
//
//        for (BillRequest.ItemRequest itemReq : request.getItems()) {
//            Product product = productRepository.findById(itemReq.getProductId()).get(); // We know it exists from the check above
//
//            BillItem item = new BillItem();
//            item.setBill(bill);
//            item.setProduct(product);
//            item.setUnitType(itemReq.getUnitType());
//            item.setQuantity(itemReq.getQuantity());
//            item.setDiscountPercent(itemReq.getDiscountPercent());
//
//            // Snapshot product prices at the time of sale
//            item.setUnitPriceSnapshot(product.getSellingPrice());
//            item.setUnitCostSnapshot(product.getCostPrice());
//            
//            int totalPieces = calculatePieces(product, itemReq.getUnitType(), itemReq.getQuantity());
//            item.setTotalPieces(totalPieces);
//
//            BigDecimal quantityBD = BigDecimal.valueOf(itemReq.getQuantity());
//            BigDecimal grossLineTotal = product.getSellingPrice().multiply(quantityBD);
//            BigDecimal discountAmount = grossLineTotal.multiply(itemReq.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);
//
//            item.setLineDiscount(discountAmount);
//            item.setLineTotal(netLineTotal);
//            billItems.add(item);
//
//            // Aggregate totals for the main bill
//            subTotal = subTotal.add(grossLineTotal);
//            totalDiscount = totalDiscount.add(discountAmount);
//            totalCostBasis = totalCostBasis.add(product.getCostPrice().multiply(BigDecimal.valueOf(totalPieces)));
//
//            // Reduce product stock from inventory
//            reduceProductStock(product, itemReq.getUnitType(), itemReq.getQuantity(), totalPieces);
//        }
//
//        bill.setItems(billItems);
//        bill.setSubTotal(subTotal);
//        bill.setTotalDiscount(totalDiscount);
//        bill.setTotal(subTotal.subtract(totalDiscount));
//        bill.setTotalCostBasis(totalCostBasis);
//
//        // STEP 3: Correctly handle customer dues
//        BigDecimal previousDue = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;
//        bill.setPreviousDue(previousDue);
//        
//        BigDecimal paymentAgainstDue = request.getPaymentAgainstPreviousDue() != null ? request.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
//        bill.setPaymentAgainstPreviousDue(paymentAgainstDue);
//        
//        // New due = Previous Due - Payment + Current Bill Total
//        BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
//        bill.setNewDue(newDue);
//        
//        // Update the customer's balance
//        customer.setDue(newDue);
//        customerRepository.save(customer);
//
//        return billRepository.save(bill);
//    }
//    
//    /**
//     * Helper method to calculate the total number of individual pieces sold.
//     */
//    private int calculatePieces(Product product, UnitType unitType, int quantity) {
//        if (product.getUnitType() == UnitType.PCS) {
//            switch (unitType) {
//                case PCS: return quantity;
//                case DOZEN: return quantity * 12;
//                case HALF_DOZEN: return quantity * 6;
//                case BOX: return quantity * (product.getUnitsPerBox() != null ? product.getUnitsPerBox() : 1);
//                case KG: return quantity;
//                case BAG: return quantity;
//            }
//        } else if (product.getUnitType() == UnitType.KG) {
//            // Add logic for KG-based products if necessary
//            return quantity; 
//        }
//        return quantity;
//    }
//
//    /**
//     * Helper method to reduce product stock from inventory.
//     */
//    private void reduceProductStock(Product product, UnitType unitType, int quantity, int totalPieces) {
//        if (product.getUnitType() == UnitType.PCS) {
//            if (unitType == UnitType.BOX) {
//                 product.setNumberOfBoxes(product.getNumberOfBoxes() - quantity);
//            }
//            // The total number of loose pieces is always the source of truth for stock.
//            // (numberOfBoxes * unitsPerBox) + loosePcs
//            Integer currentStock = product.getStockPieces();
//            Integer remainingStock = currentStock - totalPieces;
//            
//            // Recalculate boxes and loose pieces
//            if (product.getUnitsPerBox() != null && product.getUnitsPerBox() > 0) {
//                product.setNumberOfBoxes(remainingStock / product.getUnitsPerBox());
//                product.setPcs(remainingStock % product.getUnitsPerBox());
//            } else {
//                product.setNumberOfBoxes(0);
//                product.setPcs(remainingStock);
//            }
//
//        } else if (product.getUnitType() == UnitType.KG) {
//            // Logic for reducing KG/Bag stock
//            if (unitType == UnitType.BAG) {
//                product.setTotalBags(product.getTotalBags() - quantity);
//            }
//            // Add other KG stock reduction logic as needed
//        }
//        productRepository.save(product);
//    }
//
//    /**
//     * Admin-side update of a bill: adjust per-item discounts and payment against previous due,
//     * then recalculate bill totals and customer's due.
//     */
//    @Transactional
//    public Bill adminUpdateBill(UpdateBillRequest request) {
//        Bill bill = billRepository.findById(request.billId)
//                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + request.billId));
//
//        // Update payment against previous due if provided
//        if (request.paymentAgainstPreviousDue != null) {
//            bill.setPaymentAgainstPreviousDue(request.paymentAgainstPreviousDue);
//        }
//
//        // Map itemId -> new discount and apply
//        if (request.items != null && !request.items.isEmpty()) {
//            java.util.Map<Long, java.math.BigDecimal> itemIdToDiscount = new java.util.HashMap<>();
//            for (UpdateBillItemRequest i : request.items) {
//                if (i != null && i.itemId != null && i.discountPercent != null) {
//                    itemIdToDiscount.put(i.itemId, i.discountPercent);
//                }
//            }
//
//            for (BillItem item : bill.getItems()) {
//                java.math.BigDecimal newDiscount = itemIdToDiscount.get(item.getId());
//                if (newDiscount != null) {
//                    item.setDiscountPercent(newDiscount);
//                }
//
//                // Recalculate line totals
//                java.math.BigDecimal quantityBD = java.math.BigDecimal.valueOf(item.getQuantity());
//                java.math.BigDecimal grossLineTotal = item.getUnitPriceSnapshot().multiply(quantityBD);
//                java.math.BigDecimal discountAmount = grossLineTotal
//                        .multiply(item.getDiscountPercent() == null ? java.math.BigDecimal.ZERO : item.getDiscountPercent())
//                        .divide(new java.math.BigDecimal("100"), 2, RoundingMode.HALF_UP);
//                java.math.BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);
//
//                item.setLineDiscount(discountAmount);
//                item.setLineTotal(netLineTotal);
//            }
//        }
//
//        // Recalculate bill totals
//        java.math.BigDecimal subTotal = java.math.BigDecimal.ZERO;
//        java.math.BigDecimal totalDiscount = java.math.BigDecimal.ZERO;
//        for (BillItem item : bill.getItems()) {
//            java.math.BigDecimal quantityBD = java.math.BigDecimal.valueOf(item.getQuantity());
//            subTotal = subTotal.add(item.getUnitPriceSnapshot().multiply(quantityBD));
//            totalDiscount = totalDiscount.add(item.getLineDiscount());
//        }
//        bill.setSubTotal(subTotal);
//        bill.setTotalDiscount(totalDiscount);
//        bill.setTotal(subTotal.subtract(totalDiscount));
//
//        // Recompute new due and update customer due
//        java.math.BigDecimal previousDue = bill.getPreviousDue() != null ? bill.getPreviousDue() : java.math.BigDecimal.ZERO;
//        java.math.BigDecimal paymentAgainstDue = bill.getPaymentAgainstPreviousDue() != null ? bill.getPaymentAgainstPreviousDue() : java.math.BigDecimal.ZERO;
//        java.math.BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
//        bill.setNewDue(newDue);
//
//        Customer customer = bill.getCustomer();
//        if (customer != null) {
//            customer.setDue(newDue);
//            customerRepository.save(customer);
//        }
//
//        return billRepository.save(bill);
//    }
//
//    // DTOs used by AdminController for updating bills
//    public static class UpdateBillRequest {
//        public Long billId;
//        public java.math.BigDecimal paymentAgainstPreviousDue;
//        public java.util.List<UpdateBillItemRequest> items;
//    }
//
//    public static class UpdateBillItemRequest {
//        public Long itemId;
//        public java.math.BigDecimal discountPercent;
//    }
//} 

//package com.billingsolutions.service;
//
//import com.billingsolutions.model.*;
//import com.billingsolutions.repository.*;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.HashMap;
//
//@Service
//public class BillingService {
//    private final BillRepository billRepository;
//    private final ProductRepository productRepository;
//    private final CustomerRepository customerRepository;
//    private final SalesmanRepository salesmanRepository;
//
//    public BillingService(BillRepository billRepository,
//                          ProductRepository productRepository,
//                          CustomerRepository customerRepository,
//                          SalesmanRepository salesmanRepository) {
//        this.billRepository = billRepository;
//        this.productRepository = productRepository;
//        this.customerRepository = customerRepository;
//        this.salesmanRepository = salesmanRepository;
//    }
//
//    public Bill getBillById(Long id) {
//        return billRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + id));
//    }
//
//    /**
//     * Creates a new bill, validates stock and profitability, and updates inventory.
//     * This is a transactional method, ensuring all database operations succeed or fail together.
//     */
//    @Transactional
//    public Bill createBill(BillRequest request) {
//        // STEP 1: Fetch all required entities first
//        Customer customer = customerRepository.findById(request.getCustomerId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Customer ID: " + request.getCustomerId()));
//        Salesman salesman = salesmanRepository.findById(request.getSalesmanId().intValue())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Salesman ID: " + request.getSalesmanId()));
//
//        Bill bill = new Bill();
//        bill.setCustomer(customer);
//        bill.setSalesman(salesman);
//        bill.setCustomerNameSnapshot(customer.getName());
//        bill.setCustomerPhoneSnapshot(customer.getPhone());
//        bill.setCustomerAddressSnapshot(customer.getAddress());
//        bill.setCustomerGstSnapshot(customer.getGst());
//
//        BigDecimal subTotal = BigDecimal.ZERO;
//        BigDecimal totalDiscount = BigDecimal.ZERO;
//        BigDecimal totalCostBasis = BigDecimal.ZERO;
//
//        // STEP 2: Process each item, validate stock, and calculate totals
//        for (BillRequest.ItemRequest itemReq : request.getItems()) {
//            Product product = productRepository.findById(itemReq.getProductId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + itemReq.getProductId()));
//
//            // A) Calculate effective pieces and validate stock
//            int piecesSold = calculateEffectivePieces(product, itemReq.getUnitType(), itemReq.getQuantity());
//            validateAndReduceStock(product, piecesSold, itemReq.getUnitType());
//
//            // B) Validate profitability
//            validateProfitability(product, itemReq.getDiscountPercent());
//
//            // C) Create and populate the BillItem
//            BillItem item = new BillItem();
//            item.setBill(bill);
//            item.setProduct(product);
//            item.setUnitType(itemReq.getUnitType());
//            item.setQuantity(itemReq.getQuantity());
//            item.setDiscountPercent(itemReq.getDiscountPercent());
//            item.setUnitPriceSnapshot(product.getSellingPrice());
//            item.setUnitCostSnapshot(product.getCostPrice());
//            item.setTotalPieces(piecesSold);
//
//            // D) Calculate line totals based on effective pieces
//            BigDecimal effectiveQuantityBD = new BigDecimal(piecesSold);
//            BigDecimal grossLineTotal = product.getSellingPrice().multiply(effectiveQuantityBD);
//            BigDecimal discountAmount = grossLineTotal.multiply(itemReq.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);
//
//            item.setLineDiscount(discountAmount);
//            item.setLineTotal(netLineTotal);
//            bill.addItem(item); // Use the helper method to establish the bidirectional link
//
//            // E) Aggregate totals for the main bill
//            subTotal = subTotal.add(grossLineTotal);
//            totalDiscount = totalDiscount.add(discountAmount);
//            totalCostBasis = totalCostBasis.add(product.getCostPrice().multiply(effectiveQuantityBD));
//        }
//
//        bill.setSubTotal(subTotal);
//        bill.setTotalDiscount(totalDiscount);
//        bill.setTotal(subTotal.subtract(totalDiscount));
//        bill.setTotalCostBasis(totalCostBasis);
//
//        // STEP 3: Handle customer dues
//        BigDecimal previousDue = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;
//        bill.setPreviousDue(previousDue);
//        
//        BigDecimal paymentAgainstDue = request.getPaymentAgainstPreviousDue() != null ? request.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
//        bill.setPaymentAgainstPreviousDue(paymentAgainstDue);
//        
//        BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
//        bill.setNewDue(newDue);
//        
//        customer.setDue(newDue);
//        // No need to call customerRepository.save(customer) here.
//        // The @Transactional annotation will automatically save changes to the managed 'customer' entity.
//
//        return billRepository.save(bill);
//    }
//    
//    /**
//     * Calculates the total number of individual pieces being sold.
//     */
//    private int calculateEffectivePieces(Product product, UnitType unitType, int quantity) {
//        if (product.getUnitType() == UnitType.PCS) {
//            switch (unitType) {
//                case PCS: return quantity;
//                case DOZEN: return quantity * 12;
//                case HALF_DOZEN: return quantity * 6;
//                case BOX: return quantity * (product.getUnitsPerBox() != null ? product.getUnitsPerBox() : 1);
//            }
//        } else if (product.getUnitType() == UnitType.KG) {
//            BigDecimal weightPerItem = product.getWeightPerItem();
//            if (weightPerItem == null || weightPerItem.compareTo(BigDecimal.ZERO) <= 0) {
//                throw new IllegalStateException("Product '" + product.getName() + "' is KG-based but has invalid weight per item defined.");
//            }
//
//            BigDecimal quantityBD = new BigDecimal(quantity);
//            switch (unitType) {
//                case PCS: return quantity;
//                case KG:
//                    // Convert total KG sold to number of pieces
//                    return quantityBD.divide(weightPerItem, 0, RoundingMode.HALF_UP).intValue();
//                case BAG:
//                    // Convert number of bags to total KG, then to pieces
//                    BigDecimal totalWeight = quantityBD.multiply(product.getTotalBagWeight());
//                    return totalWeight.divide(weightPerItem, 0, RoundingMode.HALF_UP).intValue();
//            }
//        }
//        return quantity;
//    }
//
//    /**
//     * Checks for sufficient stock, then reduces it. Throws an exception if stock is insufficient.
//     */
//    private void validateAndReduceStock(Product product, int piecesSold, UnitType soldAsUnit) {
//        if (product.getUnitType() == UnitType.PCS) {
//            int currentStock = product.getStockPieces();
//            if (currentStock < piecesSold) {
//                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + currentStock + ", Requested: " + piecesSold);
//            }
//            int remainingStock = currentStock - piecesSold;
//
//            if (product.getUnitsPerBox() != null && product.getUnitsPerBox() > 0) {
//                product.setNumberOfBoxes(remainingStock / product.getUnitsPerBox());
//                product.setPcs(remainingStock % product.getUnitsPerBox());
//            } else {
//                product.setNumberOfBoxes(0);
//                product.setPcs(remainingStock);
//            }
//        } else if (product.getUnitType() == UnitType.KG) {
//            // For KG products, we track stock by total weight.
//            BigDecimal currentTotalWeight = product.getTotalWeight();
//            BigDecimal weightSold = product.getWeightPerItem().multiply(new BigDecimal(piecesSold));
//
//            if (currentTotalWeight.compareTo(weightSold) < 0) {
//                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + currentTotalWeight + " KG, Requested: " + weightSold + " KG");
//            }
//            BigDecimal remainingWeight = currentTotalWeight.subtract(weightSold);
//
//            // Recalculate total bags based on remaining weight
//            if (product.getTotalBagWeight() != null && product.getTotalBagWeight().compareTo(BigDecimal.ZERO) > 0) {
//                product.setTotalBags(remainingWeight.divide(product.getTotalBagWeight(), 0, RoundingMode.DOWN).intValue());
//            } else {
//                product.setTotalBags(0); // or handle as an error if a bag must have weight
//            }
//        }
//        // No need to call productRepository.save(product).
//        // @Transactional will handle saving the updated product state.
//    }
//    
//    /**
//     * Checks if an item is being sold at a profit. Throws an exception if not.
//     */
//    private void validateProfitability(Product product, BigDecimal discountPercent) {
//        BigDecimal sellingPrice = product.getSellingPrice();
//        BigDecimal costPrice = product.getCostPrice();
//        BigDecimal effectiveDiscount = discountPercent != null ? discountPercent : BigDecimal.ZERO;
//
//        BigDecimal discountAmount = sellingPrice.multiply(effectiveDiscount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//        BigDecimal finalSellingPrice = sellingPrice.subtract(discountAmount);
//
//        if (finalSellingPrice.compareTo(costPrice) < 0) {
//            throw new InsufficientProfitException(
//                "Profit Alert: Product '" + product.getName() + "' is being sold at a loss. Bill creation denied."
//            );
//        }
//    }
//
//    // You will need to create these custom exception classes
//    public static class InsufficientStockException extends RuntimeException {
//        public InsufficientStockException(String message) {
//            super(message);
//        }
//    }
//
//    public static class InsufficientProfitException extends RuntimeException {
//        public InsufficientProfitException(String message) {
//            super(message);
//        }
//    }
//    
//    // The rest of your service class (adminUpdateBill, DTOs, etc.) can remain as it was.
//    // ...
//}


package com.billingsolutions.service;

import com.billingsolutions.model.*;
import com.billingsolutions.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class BillingService {
    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
//    private final SalesmanRepository salesmanRepository;
    private final UserRepository userRepository;

    public BillingService(BillRepository billRepository,
                          ProductRepository productRepository,
                          CustomerRepository customerRepository,
                          UserRepository userRepository) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + id));
    }

    /**
     * Creates a new bill, validates stock and profitability, and updates inventory.
     * This is a transactional method, ensuring all database operations succeed or fail together.
     */
    public static class CreditLimitExceededException extends RuntimeException {
        public CreditLimitExceededException(String message) { super(message); }
    }
        
    
    @Transactional
    public Bill createBill(BillRequest request) {
        // STEP 1: Fetch all required entities first
    	
    	
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Customer ID: " + request.getCustomerId()));
        User salesmanUser = null; // Salesman can be optional
        
        
        String selectedFinancialYear = request.getFinancialYear();
//        if (selectedFinancialYear == null || selectedFinancialYear.isBlank()) {
//            throw new IllegalArgumentException("Financial Year must be selected.");
//        }
        long nextBillNumber = billRepository.findTopByFinancialYearOrderByIdDesc(selectedFinancialYear)
                .map(latestBill -> { // Use the Optional's map function for cleaner logic
                    String lastBillNo = latestBill.getBillNo();
                    try {
                        // 2. Parse the number from the last bill (e.g., "GST-1" -> 1)
                        long lastNumber = Long.parseLong(lastBillNo.substring(lastBillNo.lastIndexOf('-') + 1));
                        // 3. Increment to get the next number
                        return lastNumber + 1;
                    } catch (Exception e) {
                        // If parsing fails for any reason, safely reset to 1
                        return 1L; 
                    }
                })
                // 4. If no bill is found for that year, default to 1.
                .orElse(1L);
        
        if (request.getSalesmanId() != null) {
            salesmanUser = userRepository.findById(request.getSalesmanId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Salesman ID: " + request.getSalesmanId()));
        }

        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setSalesman(salesmanUser);
        bill.setFinancialYear(selectedFinancialYear);
//        Optional<Bill> latestBillOpt = billRepository.findTopByOrderByIdDesc();
//        long nextBillNumber = 1;
//
//        if (latestBillOpt.isPresent()) {
//            // 2. If a bill exists, parse its number (e.g., get 1 from "GST-1").
//            String lastBillNo = latestBillOpt.get().getBillNo();
//            try {
//                long lastNumber = Long.parseLong(lastBillNo.replace("GST-", ""));
//                // 3. Increment it to get the next number.
//                nextBillNumber = lastNumber + 1;
//            } catch (NumberFormatException e) {
//                // This is a fallback in case a bill number is in an unexpected format.
//                nextBillNumber = billRepository.count() + 1;
//            }
//        }
        
        // 4. Set the new bill's number with the "GST-" prefix.
        bill.setBillNo("GST-" + nextBillNumber);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // Set the username of the currently logged-in user on the bill.
            bill.setCreatedBy(authentication.getName());
        }
        
        bill.setCustomerNameSnapshot(customer.getName());
        bill.setCustomerPhoneSnapshot(customer.getPhone());
        bill.setCustomerAddressSnapshot(customer.getAddress());
        bill.setCustomerGstSnapshot(customer.getGst());

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        // STEP 2: Process each item, validate stock, and calculate totals
        for (BillRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + itemReq.getProductId()));

            // A) Calculate effective pieces
            int piecesSold = calculateEffectivePieces(product, itemReq.getUnitType(), itemReq.getQuantity());

            // B) Validate and reduce stock
            validateAndReduceStock(product, piecesSold);

            // C) Validate profitability
            validateProfitability(product, itemReq.getDiscountPercent());

            // D) Create and populate the BillItem
            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProduct(product);
            item.setUnitType(itemReq.getUnitType());
            item.setQuantity(itemReq.getQuantity());
            item.setDiscountPercent(itemReq.getDiscountPercent());
            item.setUnitPriceSnapshot(product.getSellingPrice());
            item.setUnitCostSnapshot(product.getCostPrice());
            item.setTotalPieces(piecesSold);

            // E) Calculate line totals based on effective pieces
            BigDecimal effectiveQuantityBD = new BigDecimal(piecesSold);
            BigDecimal grossLineTotal = product.getSellingPrice().multiply(effectiveQuantityBD);
            BigDecimal discountAmount = grossLineTotal.multiply(itemReq.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);

            item.setLineDiscount(discountAmount);
            item.setLineTotal(netLineTotal);
            bill.addItem(item);

            // F) Aggregate totals for the main bill
            subTotal = subTotal.add(grossLineTotal);
            totalDiscount = totalDiscount.add(discountAmount);
            totalCostBasis = totalCostBasis.add(product.getCostPrice().multiply(effectiveQuantityBD));
        }

        bill.setSubTotal(subTotal);
        bill.setTotalDiscount(totalDiscount);
        bill.setTotal(subTotal.subtract(totalDiscount));
        bill.setTotalCostBasis(totalCostBasis);

        // STEP 3: Handle customer dues
        BigDecimal previousDue = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;
        bill.setPreviousDue(previousDue);
        
        BigDecimal paymentAgainstDue = request.getPaymentAgainstPreviousDue() != null ? request.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
        bill.setPaymentAgainstPreviousDue(paymentAgainstDue);
        
        BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
        bill.setNewDue(newDue);
        
        BigDecimal creditLimit = customer.getCreditLimit();
        if (creditLimit != null && newDue.compareTo(creditLimit) > 0) {
            throw new CreditLimitExceededException(
                "Cannot create bill. New due amount of " + newDue.setScale(2, RoundingMode.HALF_UP) +
                " would exceed the customer's credit limit of " + creditLimit.setScale(2, RoundingMode.HALF_UP) + "."
            );
        }
        	
        customer.setDue(newDue);

        return billRepository.save(bill);
    }
    
    /**
     * Calculates the total number of individual pieces being sold.
     */
    private int calculateEffectivePieces(Product product, UnitType unitType, int quantity) {
        if (product.getUnitType() == UnitType.PCS) {
            switch (unitType) {
                case PCS: return quantity;
                case DOZEN: return quantity * 12;
                case HALF_DOZEN: return quantity * 6;
                case BOX: return quantity * (product.getUnitsPerBox() != null ? product.getUnitsPerBox() : 1);
            }
        } else if (product.getUnitType() == UnitType.KG) {
            BigDecimal weightPerItem = product.getWeightPerItem();
            if (weightPerItem == null || weightPerItem.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Product '" + product.getName() + "' is KG-based but has invalid weight per item defined.");
            }

            BigDecimal quantityBD = new BigDecimal(quantity);
            switch (unitType) {
                case PCS: return quantity;
                case KG:
                    // Convert total KG sold to number of pieces
                    return quantityBD.divide(weightPerItem, 0, RoundingMode.HALF_UP).intValue();
                case BAG:
                    // Convert number of bags to total KG, then to pieces
                    BigDecimal totalWeight = quantityBD.multiply(product.getTotalBagWeight());
                    return totalWeight.divide(weightPerItem, 0, RoundingMode.HALF_UP).intValue();
            }
        }
        return quantity;
    }

    /**
     * Checks for sufficient stock, then reduces it. Throws an exception if stock is insufficient.
     */
//    private void validateAndReduceStock(Product product, int piecesSold) {
//        if (product.getUnitType() == UnitType.PCS) {
//            int currentStockInPieces = product.getStockPieces();
//            if (currentStockInPieces < piecesSold) {
//                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + currentStockInPieces + ", Requested: " + piecesSold);
//            }
//            int remainingStockInPieces = currentStockInPieces - piecesSold;
//
//            if (product.getUnitsPerBox() != null && product.getUnitsPerBox() > 0) {
//                product.setNumberOfBoxes(remainingStockInPieces / product.getUnitsPerBox());
//                product.setPcs(remainingStockInPieces % product.getUnitsPerBox());
//            } else {
//                product.setNumberOfBoxes(0);
//                product.setPcs(remainingStockInPieces);
//            }
//
//        } else if (product.getUnitType() == UnitType.KG) {
//            BigDecimal weightPerPiece = product.getWeightPerItem();
//            if (weightPerPiece == null || weightPerPiece.compareTo(BigDecimal.ZERO) <= 0) {
//                 throw new IllegalStateException("Cannot calculate stock for '" + product.getName() + "' because its weight per item is not defined.");
//            }
//
//            BigDecimal weightSold = weightPerPiece.multiply(new BigDecimal(piecesSold));
//            BigDecimal currentTotalWeight = product.getTotalWeight();
//
//            if (currentTotalWeight.compareTo(weightSold) < 0) {
//                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + currentTotalWeight + " KG, Requested: " + weightSold + " KG");
//            }
//            BigDecimal remainingWeight = currentTotalWeight.subtract(weightSold);
//
//            BigDecimal weightPerBag = product.getTotalBagWeight();
//            if (weightPerBag != null && weightPerBag.compareTo(BigDecimal.ZERO) > 0) {
//                product.setTotalBags(remainingWeight.divide(weightPerBag, 0, RoundingMode.DOWN).intValue());
//            } else {
//                product.setTotalBags(0);
//            }
//        }
//    }
    
    
    private void validateAndReduceStock(Product product, int piecesSold) {
        if (product.getUnitType() == UnitType.PCS) {
            int currentStockInPieces = product.getPcs();
            if (currentStockInPieces < piecesSold) {
                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + currentStockInPieces + ", Requested: " + piecesSold);
            }
            int remainingStockInPieces = currentStockInPieces - piecesSold;

            if (product.getUnitsPerBox() != null && product.getUnitsPerBox() > 0) {
                product.setNumberOfBoxes(remainingStockInPieces / product.getUnitsPerBox());
                product.setPcs(currentStockInPieces-piecesSold);
            } else {
                product.setNumberOfBoxes(0);
                product.setPcs(remainingStockInPieces);
            }

        } else if (product.getUnitType() == UnitType.KG) {
            BigDecimal weightPerPiece = product.getWeightPerItem();
            if (weightPerPiece == null || weightPerPiece.compareTo(BigDecimal.ZERO) <= 0) {
                 throw new IllegalStateException("Cannot calculate stock for '" + product.getName() + "' because its weight per item is not defined.");
            }

            BigDecimal totalPcs=new BigDecimal(product.getPcs());
            BigDecimal totalPcsSold=new BigDecimal(piecesSold);
            BigDecimal weightPerBag = product.getTotalBagWeight();
            BigDecimal weightAvailable=totalPcs.multiply(product.getWeightPerItem());
            BigDecimal pcsPerBag=product.getTotalBagWeight().divide(product.getWeightPerItem());
            if (totalPcsSold.intValue()>product.getPcs()) {
                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + totalPcs + " PCS Or" +weightAvailable+ "KG, Requested: " + piecesSold + 
                		" PCS"+totalPcsSold.multiply(product.getWeightPerItem())+" KG");
            }
            if (weightPerBag != null && weightPerBag.compareTo(BigDecimal.ZERO) > 0 ) {
               product.setTotalBags((product.getPcs()-piecesSold)/(pcsPerBag.intValue()));
               product.setPcs(product.getPcs()-piecesSold);
            } else {
                product.setTotalBags(0);
            }
        }
    }
    
    
    
    /**
     * Checks if an item is being sold at a profit. Throws an exception if not.
     */
    private void validateProfitability(Product product, BigDecimal discountPercent) {
        BigDecimal sellingPrice = product.getSellingPrice();
        BigDecimal costPrice = product.getCostPrice();
        BigDecimal effectiveDiscount = discountPercent != null ? discountPercent : BigDecimal.ZERO;

        BigDecimal discountAmount = sellingPrice.multiply(effectiveDiscount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalSellingPrice = sellingPrice.subtract(discountAmount);

        if (finalSellingPrice.compareTo(costPrice) < 0) {
            throw new InsufficientProfitException(
                "Profit Alert: Product '" + product.getName() + "' is being sold at a loss. Bill creation denied."
            );
        }
    }

    // Custom exception classes (you should create these as separate files in a real project)
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }

    public static class InsufficientProfitException extends RuntimeException {
        public InsufficientProfitException(String message) {
            super(message);
        }
    }
    
    /**
     * Admin-side update of a bill.
     */
    @Transactional
    public Bill adminUpdateBill(UpdateBillRequest request) {
        Bill bill = billRepository.findById(request.billId)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + request.billId));

        if (request.paymentAgainstPreviousDue != null) {
            bill.setPaymentAgainstPreviousDue(request.paymentAgainstPreviousDue);
        }

        if (request.items != null && !request.items.isEmpty()) {
            Map<Long, BigDecimal> itemIdToDiscount = new HashMap<>();
            for (UpdateBillItemRequest i : request.items) {
                if (i != null && i.itemId != null && i.discountPercent != null) {
                    itemIdToDiscount.put(i.itemId, i.discountPercent);
                }
            }

            for (BillItem item : bill.getItems()) {
                BigDecimal newDiscount = itemIdToDiscount.get(item.getId());
                if (newDiscount != null) {
                    item.setDiscountPercent(newDiscount);
                }

                BigDecimal quantityBD = BigDecimal.valueOf(item.getQuantity());
                BigDecimal grossLineTotal = item.getUnitPriceSnapshot().multiply(quantityBD);
                BigDecimal discountAmount = grossLineTotal
                        .multiply(item.getDiscountPercent() == null ? BigDecimal.ZERO : item.getDiscountPercent())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);

                item.setLineDiscount(discountAmount);
                item.setLineTotal(netLineTotal);
            }
        }

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (BillItem item : bill.getItems()) {
            BigDecimal quantityBD = BigDecimal.valueOf(item.getQuantity());
            subTotal = subTotal.add(item.getUnitPriceSnapshot().multiply(quantityBD));
            totalDiscount = totalDiscount.add(item.getLineDiscount());
        }
        bill.setSubTotal(subTotal);
        bill.setTotalDiscount(totalDiscount);
        bill.setTotal(subTotal.subtract(totalDiscount));

        BigDecimal previousDue = bill.getPreviousDue() != null ? bill.getPreviousDue() : BigDecimal.ZERO;
        BigDecimal paymentAgainstDue = bill.getPaymentAgainstPreviousDue() != null ? bill.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
        BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
        bill.setNewDue(newDue);

        Customer customer = bill.getCustomer();
        if (customer != null) {
            customer.setDue(newDue);
        }

        return billRepository.save(bill);
    }

    // DTOs for the admin update feature
    public static class UpdateBillRequest {
        public Long billId;
        public BigDecimal paymentAgainstPreviousDue;
        public List<UpdateBillItemRequest> items;
    }

    public static class UpdateBillItemRequest {
        public Long itemId;
        public BigDecimal discountPercent;
    }
}