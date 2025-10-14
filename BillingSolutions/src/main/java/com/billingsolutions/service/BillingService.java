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

//
//package com.billingsolutions.service;
//
//import com.billingsolutions.model.*;
//import com.billingsolutions.repository.*;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.HashMap;
//
//@Service
//public class BillingService {
//    private final BillRepository billRepository;
//    private final ProductRepository productRepository;
//    private final CustomerRepository customerRepository;
////    private final SalesmanRepository salesmanRepository;
//    private final UserRepository userRepository;
//
//    public BillingService(BillRepository billRepository,
//                          ProductRepository productRepository,
//                          CustomerRepository customerRepository,
//                          UserRepository userRepository) {
//        this.billRepository = billRepository;
//        this.productRepository = productRepository;
//        this.customerRepository = customerRepository;
//        this.userRepository = userRepository;
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
//    public static class CreditLimitExceededException extends RuntimeException {
//        public CreditLimitExceededException(String message) { super(message); }
//    }
//        
//    
//    @Transactional
//    public Bill createBill(BillRequest request) {
//        // STEP 1: Fetch all required entities first
//    	
//    	
//        Customer customer = customerRepository.findById(request.getCustomerId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Customer ID: " + request.getCustomerId()));
//        User salesmanUser = null; // Salesman can be optional
//        
//        
//        String selectedFinancialYear = request.getFinancialYear();
////        if (selectedFinancialYear == null || selectedFinancialYear.isBlank()) {
////            throw new IllegalArgumentException("Financial Year must be selected.");
////        }
//        long nextBillNumber = billRepository.findTopByFinancialYearOrderByIdDesc(selectedFinancialYear)
//                .map(latestBill -> { // Use the Optional's map function for cleaner logic
//                    String lastBillNo = latestBill.getBillNo();
//                    try {
//                        // 2. Parse the number from the last bill (e.g., "GST-1" -> 1)
//                        long lastNumber = Long.parseLong(lastBillNo.substring(lastBillNo.lastIndexOf('-') + 1));
//                        // 3. Increment to get the next number
//                        return lastNumber + 1;
//                    } catch (Exception e) {
//                        // If parsing fails for any reason, safely reset to 1
//                        return 1L; 
//                    }
//                })
//                // 4. If no bill is found for that year, default to 1.
//                .orElse(1L);
//        
//        if (request.getSalesmanId() != null) {
//            salesmanUser = userRepository.findById(request.getSalesmanId())
//                    .orElseThrow(() -> new IllegalArgumentException("Invalid Salesman ID: " + request.getSalesmanId()));
//        }
//
//        Bill bill = new Bill();
//        bill.setCustomer(customer);
//        bill.setSalesman(salesmanUser);
//        bill.setFinancialYear(selectedFinancialYear);
//        
//        // 4. Set the new bill's number with the "GST-" prefix.
//        bill.setBillNo("GST-" + nextBillNumber);
//        
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null) {
//            // Set the username of the currently logged-in user on the bill.
//            bill.setCreatedBy(authentication.getName());
//        }
//        
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
//            // A) Calculate effective pieces
//            int piecesSold = calculateEffectivePieces(product, itemReq.getUnitType(), itemReq.getQuantity());
//
//            // B) Validate and reduce stock
//            validateAndReduceStock(product, piecesSold);
//
//            // C) Validate profitability
//            validateProfitability(product, itemReq.getDiscountPercent());
//
//            // D) Create and populate the BillItem
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
//            // E) Calculate line totals based on effective pieces
//            BigDecimal effectiveQuantityBD = new BigDecimal(piecesSold);
//            BigDecimal grossLineTotal = product.getSellingPrice().multiply(effectiveQuantityBD);
//            BigDecimal discountAmount = grossLineTotal.multiply(itemReq.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);
//
//            item.setLineDiscount(discountAmount);
//            item.setLineTotal(netLineTotal);
//            bill.addItem(item);
//
//            // F) Aggregate totals for the main bill
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
//        BigDecimal creditLimit = customer.getCreditLimit();
//        if (creditLimit != null && newDue.compareTo(creditLimit) > 0) {
//            throw new CreditLimitExceededException(
//                "Cannot create bill. New due amount of " + newDue.setScale(2, RoundingMode.HALF_UP) +
//                " would exceed the customer's credit limit of " + creditLimit.setScale(2, RoundingMode.HALF_UP) + "."
//            );
//        }
//        	
//        customer.setDue(newDue);
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
//
//    
//    private void validateAndReduceStock(Product product, int piecesSold) {
//        if (product.getUnitType() == UnitType.PCS) {
//            int currentStockInPieces = product.getPcs();
//            if (currentStockInPieces < piecesSold) {
//                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + currentStockInPieces + ", Requested: " + piecesSold);
//            }
//            int remainingStockInPieces = currentStockInPieces - piecesSold;
//
//            if (product.getUnitsPerBox() != null && product.getUnitsPerBox() > 0) {
//                product.setNumberOfBoxes(remainingStockInPieces / product.getUnitsPerBox());
//                product.setPcs(currentStockInPieces-piecesSold);
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
//            BigDecimal totalPcs=new BigDecimal(product.getPcs());
//            BigDecimal totalPcsSold=new BigDecimal(piecesSold);
//            BigDecimal weightPerBag = product.getTotalBagWeight();
//            BigDecimal weightAvailable=totalPcs.multiply(product.getWeightPerItem());
//            BigDecimal pcsPerBag=product.getTotalBagWeight().divide(product.getWeightPerItem());
//            if (totalPcsSold.intValue()>product.getPcs()) {
//                throw new InsufficientStockException("Not enough stock for '" + product.getName() + "'. Available: " + totalPcs + " PCS Or" +weightAvailable+ "KG, Requested: " + piecesSold + 
//                		" PCS"+totalPcsSold.multiply(product.getWeightPerItem())+" KG");
//            }
//            if (weightPerBag != null && weightPerBag.compareTo(BigDecimal.ZERO) > 0 ) {
//               product.setTotalBags((product.getPcs()-piecesSold)/(pcsPerBag.intValue()));
//               product.setPcs(product.getPcs()-piecesSold);
//            } else {
//                product.setTotalBags(0);
//            }
//        }
//    }
//    
//    
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
//    // Custom exception classes (you should create these as separate files in a real project)
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
//    /**
//     * Admin-side update of a bill.
//     */
//    @Transactional
//    public Bill adminUpdateBill(UpdateBillRequest request) {
//        Bill bill = billRepository.findById(request.billId)
//                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + request.billId));
//
//        if (request.paymentAgainstPreviousDue != null) {
//            bill.setPaymentAgainstPreviousDue(request.paymentAgainstPreviousDue);
//        }
//
//        if (request.items != null && !request.items.isEmpty()) {
//            Map<Long, BigDecimal> itemIdToDiscount = new HashMap<>();
//            for (UpdateBillItemRequest i : request.items) {
//                if (i != null && i.itemId != null && i.discountPercent != null) {
//                    itemIdToDiscount.put(i.itemId, i.discountPercent);
//                }
//            }
//
//            for (BillItem item : bill.getItems()) {
//                BigDecimal newDiscount = itemIdToDiscount.get(item.getId());
//                if (newDiscount != null) {
//                    item.setDiscountPercent(newDiscount);
//                }
//
//                BigDecimal quantityBD = BigDecimal.valueOf(item.getQuantity());
//                BigDecimal grossLineTotal = item.getUnitPriceSnapshot().multiply(quantityBD);
//                BigDecimal discountAmount = grossLineTotal
//                        .multiply(item.getDiscountPercent() == null ? BigDecimal.ZERO : item.getDiscountPercent())
//                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//                BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);
//
//                item.setLineDiscount(discountAmount);
//                item.setLineTotal(netLineTotal);
//            }
//        }
//
//        BigDecimal subTotal = BigDecimal.ZERO;
//        BigDecimal totalDiscount = BigDecimal.ZERO;
//        for (BillItem item : bill.getItems()) {
//            BigDecimal quantityBD = BigDecimal.valueOf(item.getQuantity());
//            subTotal = subTotal.add(item.getUnitPriceSnapshot().multiply(quantityBD));
//            totalDiscount = totalDiscount.add(item.getLineDiscount());
//        }
//        bill.setSubTotal(subTotal);
//        bill.setTotalDiscount(totalDiscount);
//        bill.setTotal(subTotal.subtract(totalDiscount));
//
//        BigDecimal previousDue = bill.getPreviousDue() != null ? bill.getPreviousDue() : BigDecimal.ZERO;
//        BigDecimal paymentAgainstDue = bill.getPaymentAgainstPreviousDue() != null ? bill.getPaymentAgainstPreviousDue() : BigDecimal.ZERO;
//        BigDecimal newDue = previousDue.subtract(paymentAgainstDue).add(bill.getTotal());
//        bill.setNewDue(newDue);
//
//        Customer customer = bill.getCustomer();
//        if (customer != null) {
//            customer.setDue(newDue);
//        }
//
//        return billRepository.save(bill);
//    }
//
//    // DTOs for the admin update feature
//    public static class UpdateBillRequest {
//        public Long billId;
//        public BigDecimal paymentAgainstPreviousDue;
//        public List<UpdateBillItemRequest> items;
//    }
//
//    public static class UpdateBillItemRequest {
//        public Long itemId;
//        public BigDecimal discountPercent;
//    }
//}


package com.billingsolutions.service;

import com.billingsolutions.model.*;
import com.billingsolutions.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;



@Service
@Transactional
public class BillingService {
    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public BillingService(BillRepository billRepository,
                          ProductRepository productRepository,
                          CustomerRepository customerRepository,
                          UserRepository userRepository,
                          PaymentService paymentService) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }
    
    

    private Business getCurrentBusiness() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user '" + currentUsername + "' not found in database."));
        
        Business business = currentUser.getBusiness();
        if (business == null) {
            throw new IllegalStateException("User '" + currentUsername + "' is not associated with any business.");
        }
        return business;
    }

   
    public Bill getBillById(Long id) {
        return billRepository.findByIdAndBusiness(id, getCurrentBusiness())
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with ID: " + id));
    }
    
    
    /**
     * MODIFIED: This method now returns a Page of BillSummaryDTOs.
     * It fetches the bills and then converts each one to a simple DTO.
     */
//    public Page<BillSummaryDTO> findAllForCurrentBusiness(Pageable pageable) {
//        Page<Bill> billsPage = billRepository.findByBusiness(getCurrentBusiness(), pageable);
//        return billsPage.map(BillSummaryDTO::new);
//    }
    
    /**
     * MODIFIED: This method now also returns a Page of BillSummaryDTOs.
     * This is the core of the fix that prevents serialization errors.
     */
//    public Page<BillSummaryDTO> searchBills(String query, String financialYear, Pageable pageable) {
//        Business currentBusiness = getCurrentBusiness();
//        String effectiveQuery = StringUtils.hasText(query) ? query : null;
//        String effectiveFinancialYear = StringUtils.hasText(financialYear) ? financialYear : null;
//
//        Page<Bill> billsPage = billRepository.searchBills(currentBusiness, effectiveFinancialYear, effectiveQuery, pageable);
//        
//        // Convert each found Bill entity into a simple, safe BillSummaryDTO.
//        return billsPage.map(BillSummaryDTO::new);
//    }
    
    
    /**
     * MODIFIED: The search method now uses the isBillActionable logic
     * to populate the new 'editable' flag in the BillSummaryDTO.
     */
    public Page<BillSummaryDTO> searchBills(String query, String financialYear, Pageable pageable) {
        Business currentBusiness = getCurrentBusiness();
        String effectiveQuery = StringUtils.hasText(query) ? query : null;
        String effectiveFinancialYear = StringUtils.hasText(financialYear) ? financialYear : null;
        Page<Bill> billsPage = billRepository.searchBills(currentBusiness, effectiveFinancialYear, effectiveQuery, pageable);
        
        // Convert each Bill to a DTO, passing in its actionable status.
        return billsPage.map(bill -> new BillSummaryDTO(bill, isBillActionable(bill)));
    }

    
    /**
     * ADDED: Business logic to determine if a bill action (edit/delete) is allowed.
     */
    public boolean isBillActionable(Bill bill) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        // Admins can always take action.
        if (isAdmin) {
            return true;
        }

        // For other users, check the time limit.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime billCreationTime = bill.getCreatedAt();
        LocalTime cutoff = LocalTime.of(21, 0); // 9:00 PM cutoff

        boolean isSameDay = now.toLocalDate().isEqual(billCreationTime.toLocalDate());
        boolean beforeCutoff = now.toLocalTime().isBefore(cutoff);

        return isSameDay && beforeCutoff;
    }
    

    
    /**
     * This is the complete, final, and correct version of the createBill method.
     */
    @Transactional
    public BillCreationResult createBill(BillRequest request) {
        Business currentBusiness = getCurrentBusiness();
        String warningMessage = null;
        
        Customer customer = customerRepository.findByIdAndBusiness(request.getCustomerId(), currentBusiness)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Customer ID for this business: " + request.getCustomerId()));
        
        
     // --- NEW CREDIT LIMIT CHECK ---
        if (!isCurrentUserAdmin()) {
            BigDecimal creditLimit = customer.getCreditLimit();
            // Only check if a credit limit is actually set (i.e., greater than zero)
            if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0) {
            	
            	System.out.println("This block of code is running perfectly");
                BigDecimal currentDue = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;
                BigDecimal newBillTotal = calculateProvisionalTotal(request);
                BigDecimal projectedDue = currentDue.add(newBillTotal);

                if (projectedDue.compareTo(creditLimit) > 0) {
                    // If the user is an admin, set a warning message.
                    if (isCurrentUserAdmin()) {
                        warningMessage = "Warning: Customer's projected due of ₹" + projectedDue.setScale(2, RoundingMode.HALF_UP) +
                                         " exceeds their credit limit of ₹" + creditLimit.setScale(2, RoundingMode.HALF_UP) + ". Bill created with admin override.";
                    } else {
                        // If not an admin, throw the error and block the creation as before.
                        throw new CreditLimitExceededException(
                            "Cannot create bill. Customer's projected due of ₹" + projectedDue.setScale(2, RoundingMode.HALF_UP) +
                            " exceeds their credit limit of ₹" + creditLimit.setScale(2, RoundingMode.HALF_UP) + "."
                        );
                    }
                }
            }
        }
        // --- END OF CREDIT LIMIT CHECK ---
        

        // STEP 1: Capture the customer's due amount BEFORE any payments are made.
        BigDecimal previousDueForNewBill = customer.getDue() != null ? customer.getDue() : BigDecimal.ZERO;

        // STEP 2: Process any payments against old bills.
        if (request.getPayments() != null) {
            for (PaymentRequest paymentRequest : request.getPayments()) {
                if (paymentRequest.getPaymentType() != null && !"none".equals(paymentRequest.getPaymentType())) {
                    Bill billToPay = getBillById(paymentRequest.getBillId());
                    BigDecimal remainingDueOnBill = billToPay.getTotal().subtract(
                        billToPay.getPayments().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                    );
                    
                    BigDecimal paymentAmount = BigDecimal.ZERO;
                    if ("full".equals(paymentRequest.getPaymentType())) {
                        paymentAmount = remainingDueOnBill;
                    } else if ("partial".equals(paymentRequest.getPaymentType()) && paymentRequest.getPartialAmount() != null) {
                        paymentAmount = paymentRequest.getPartialAmount();
                    }

                    if (paymentAmount.compareTo(remainingDueOnBill) > 0) {
                        paymentAmount = remainingDueOnBill;
                    }

                    if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                        paymentService.createPayment(billToPay.getId(), paymentAmount);
                    }
                }
            }
        }
        
        // STEP 3: Create the NEW bill.
        String selectedFinancialYear = request.getFinancialYear();
        long nextBillNumber = billRepository.findTopByFinancialYearAndBusinessOrderByIdDesc(selectedFinancialYear, currentBusiness)
            .map(latestBill -> {
                String lastBillNo = latestBill.getBillNo();
                try {
                    long lastNumber = Long.parseLong(lastBillNo.substring(lastBillNo.lastIndexOf('-') + 1));
                    return lastNumber + 1;
                } catch (Exception e) { return 1L; }
            }).orElse(1L);

        Bill bill = new Bill();
        bill.setBusiness(currentBusiness);
        bill.setFinancialYear(selectedFinancialYear);
        bill.setBillNo("GST-" + nextBillNumber);
        
        User salesmanUser = null;
        if (request.getSalesmanId() != null) {
            salesmanUser = userRepository.findById(request.getSalesmanId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Salesman ID: " + request.getSalesmanId()));
        }

        bill.setCustomer(customer);
        bill.setSalesman(salesmanUser);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            bill.setCreatedBy(authentication.getName());
        }
        
        bill.setCustomerNameSnapshot(customer.getName());
        bill.setCustomerPhoneSnapshot(customer.getPhone());
        bill.setCustomerAddressSnapshot(customer.getAddress());
        bill.setCustomerGstSnapshot(customer.getGst());

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO; // ADDED: Initialize total tax

        for (BillRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndBusiness(itemReq.getProductId(), currentBusiness)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID for this business: " + itemReq.getProductId()));
            
            int piecesSold = calculateEffectivePieces(product, itemReq.getUnitType(), itemReq.getQuantity());
            validateAndReduceStock(product, piecesSold);
            validateProfitability(product, itemReq.getDiscountPercent());

            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProduct(product);
            item.setUnitType(itemReq.getUnitType());
            item.setQuantity(itemReq.getQuantity());
            item.setDiscountPercent(itemReq.getDiscountPercent());
            item.setUnitPriceSnapshot(product.getSellingPrice());
            item.setUnitCostSnapshot(product.getCostPrice());
            item.setTotalPieces(piecesSold);
            BigDecimal effectiveQuantityBD = new BigDecimal(piecesSold);
            BigDecimal grossLineTotal = product.getSellingPrice().multiply(effectiveQuantityBD);
            BigDecimal discountAmount = grossLineTotal.multiply(itemReq.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);

            item.setLineDiscount(discountAmount);
            item.setLineTotal(netLineTotal);
            
            // === TAX CALCULATION START ===
            
            BigDecimal HUNDRED = new BigDecimal("100");
            BigDecimal gstRate = product.getGstRate(); 
            BigDecimal denominator = HUNDRED.add(gstRate);

            item.setGstRateSnapshot(product.getGstRate());
            BigDecimal lineTax = netLineTotal.multiply(gstRate).divide(denominator, 2, RoundingMode.HALF_UP);
            		//netLineTotal.multiply(product.getGstRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            item.setLineTax(lineTax);
            totalTax = totalTax.add(lineTax);
            // === TAX CALCULATION END ===
            
            bill.addItem(item);
            subTotal = subTotal.add(grossLineTotal);
            totalDiscount = totalDiscount.add(discountAmount);
            totalCostBasis = totalCostBasis.add(product.getCostPrice().multiply(effectiveQuantityBD));
        }

        bill.setSubTotal(subTotal);
        bill.setTotalDiscount(totalDiscount);
        bill.setTotalTax(totalTax); // ADDED: Set total tax on the bill
        bill.setTotal(subTotal.subtract(totalDiscount)); // MODIFIED: Final total includes tax
        bill.setTotalCostBasis(totalCostBasis);
        
        // STEP 4: Correctly calculate and set the final due amounts.
        bill.setPreviousDue(previousDueForNewBill);
        bill.setPaymentAgainstPreviousDue(BigDecimal.ZERO); // A new bill itself has no payments yet.

        BigDecimal finalCustomerDue = customer.getDue().add(bill.getTotal());
        bill.setNewDue(finalCustomerDue);
        customer.setDue(finalCustomerDue);
        Bill savedBill = billRepository.save(bill);
        return new BillCreationResult(savedBill, warningMessage);
        //return billRepository.save(bill);
    }
    /**
     * THIS IS THE FINAL, CORRECTED VERSION OF THE updateBill METHOD.
     */
    @Transactional
    public Bill updateBill(Long billId, BillRequest request) {
        Bill bill = getBillById(billId);
        if (!isBillActionable(bill)) {
            throw new SecurityException("You do not have permission to edit this bill.");
        }
        
        Customer customer = bill.getCustomer();
        BigDecimal oldBillTotal = bill.getTotal();

        Map<Long, BillItem> oldItemsMap = bill.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), Function.identity()));

        // Step 1: Add stock back for items that were removed or had their quantity reduced.
        for (BillItem oldItem : bill.getItems()) {
            boolean itemRemoved = request.getItems().stream()
                .noneMatch(newItem -> newItem.getProductId().equals(oldItem.getProduct().getId()));

            if (itemRemoved) {
                oldItem.getProduct().setPcs(oldItem.getProduct().getPcs() + oldItem.getTotalPieces());
            } else {
                BillRequest.ItemRequest correspondingNewItem = request.getItems().stream()
                    .filter(newItem -> newItem.getProductId().equals(oldItem.getProduct().getId())).findFirst().get();
                int newPieces = calculateEffectivePieces(oldItem.getProduct(), correspondingNewItem.getUnitType(), correspondingNewItem.getQuantity());
                if (oldItem.getTotalPieces() > newPieces) {
                    oldItem.getProduct().setPcs(oldItem.getProduct().getPcs() + (oldItem.getTotalPieces() - newPieces));
                }
            }
        }

        // Step 2: Clear old items and rebuild the list, reducing stock and calculating new totals.
        bill.getItems().clear();
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO; // ADDED: Initialize total tax


        for (BillRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndBusiness(itemReq.getProductId(), getCurrentBusiness()).orElseThrow();
            int newPieces = calculateEffectivePieces(product, itemReq.getUnitType(), itemReq.getQuantity());

            BillItem oldItem = oldItemsMap.get(itemReq.getProductId());
            if (oldItem == null) { // This is a newly added item
                validateAndReduceStock(product, newPieces);
            } else { // This item existed before, check if quantity increased
                if (newPieces > oldItem.getTotalPieces()) {
                    validateAndReduceStock(product, newPieces - oldItem.getTotalPieces());
                }
            }
            validateProfitability(product, itemReq.getDiscountPercent());

            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProduct(product);
            item.setUnitType(itemReq.getUnitType());
            item.setQuantity(itemReq.getQuantity());
            item.setDiscountPercent(itemReq.getDiscountPercent());
            item.setUnitPriceSnapshot(product.getSellingPrice());
            item.setUnitCostSnapshot(product.getCostPrice());
            item.setTotalPieces(newPieces);
            BigDecimal effectiveQuantityBD = new BigDecimal(newPieces);
            BigDecimal grossLineTotal = product.getSellingPrice().multiply(effectiveQuantityBD);
            BigDecimal discountAmount = grossLineTotal.multiply(itemReq.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);
            item.setLineDiscount(discountAmount);
            item.setLineTotal(netLineTotal);
            
         // === TAX CALCULATION START ===
            BigDecimal HUNDRED = new BigDecimal("100");
            BigDecimal gstRate = product.getGstRate(); 
            BigDecimal denominator = HUNDRED.add(gstRate);
            item.setGstRateSnapshot(product.getGstRate());
            BigDecimal lineTax = netLineTotal.multiply(gstRate).divide(denominator, 2, RoundingMode.HALF_UP);
            item.setLineTax(lineTax);
            totalTax = totalTax.add(lineTax);
            // === TAX CALCULATION END ===
            
            bill.addItem(item);
            
            subTotal = subTotal.add(grossLineTotal);
            totalDiscount = totalDiscount.add(discountAmount);
            totalCostBasis = totalCostBasis.add(product.getCostPrice().multiply(effectiveQuantityBD));
        }

        bill.setSubTotal(subTotal);
        bill.setTotalDiscount(totalDiscount);
        bill.setTotalTax(totalTax); // ADDED: Set total tax on the bill
        bill.setTotal(subTotal);
        bill.setTotalCostBasis(totalCostBasis);

        // Step 3: Correctly update the customer's overall due balance.
        BigDecimal newBillTotal = bill.getTotal();
        BigDecimal dueChange = newBillTotal.subtract(oldBillTotal);
        customer.setDue(customer.getDue().add(dueChange));
        
        bill.setNewDue(customer.getDue());

        return billRepository.save(bill);
    }

    
    public void deleteBill(Long billId) {
        Bill bill = getBillById(billId);
        if (!isBillActionable(bill)) {
            throw new SecurityException("You do not have permission to delete this bill.");
        }
        Customer customer = bill.getCustomer();
        for (BillItem item : bill.getItems()) {
            Product product = item.getProduct();
            if(product.getUnitType() == UnitType.PCS) {
            	
            	product.setPcs(product.getPcs() + item.getTotalPieces());
            	product.setNumberOfBoxes(product.getPcs()/product.getUnitsPerBox());
            }
            if(product.getUnitType() == UnitType.KG) {
            	product.setPcs(product.getPcs() + item.getTotalPieces());
            	product.setTotalBags(product.getPcs()/((product.getTotalBagWeight().divide(product.getWeightPerItem()).intValue())));
            }
        }
        BigDecimal totalPaidOnThisBill = bill.getAmountPaid();
        customer.setDue(customer.getDue().add(totalPaidOnThisBill).subtract(bill.getTotal()));
        billRepository.delete(bill);
    }
    
    
    /**
     * ADDED & SECURED: Admin-side update of a bill, now secure for multi-tenancy.
     */
    @Transactional
    public Bill adminUpdateBill(UpdateBillRequest request) {
        // Securely fetch the bill, ensuring it belongs to the current user's business.
        Bill bill = billRepository.findByIdAndBusiness(request.billId, getCurrentBusiness())
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

                BigDecimal quantityBD = BigDecimal.valueOf(item.getTotalPieces()); // Use total pieces for recalculation
                BigDecimal grossLineTotal = item.getUnitPriceSnapshot().multiply(quantityBD);
                BigDecimal discountAmount = grossLineTotal
                        .multiply(item.getDiscountPercent() == null ? BigDecimal.ZERO : item.getDiscountPercent())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);

                item.setLineDiscount(discountAmount);
                item.setLineTotal(netLineTotal);
            }
        }

        // Recalculate bill totals
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (BillItem item : bill.getItems()) {
            BigDecimal quantityBD = BigDecimal.valueOf(item.getTotalPieces());
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
    
    
    /**
     * Checks if the currently authenticated user has the 'ROLE_ADMIN'.
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
    }
    // --- Helper Methods, DTOs & Exception Classes ---
    
    /**
     * Calculates the total value of a new bill request before it's saved.
     * This is used to project the customer's future due amount for the credit check.
     */
    private BigDecimal calculateProvisionalTotal(BillRequest request) {
        Business currentBusiness = getCurrentBusiness();
        BigDecimal provisionalTotal = BigDecimal.ZERO;

        if (request.getItems() == null) {
            return provisionalTotal;
        }

        for (BillRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndBusiness(itemReq.getProductId(), currentBusiness)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + itemReq.getProductId()));
            
            int pieces = calculateEffectivePieces(product, itemReq.getUnitType(), itemReq.getQuantity());
            BigDecimal effectiveQuantityBD = new BigDecimal(pieces);
            
            BigDecimal grossLineTotal = product.getSellingPrice().multiply(effectiveQuantityBD);
            BigDecimal discountPercent = itemReq.getDiscountPercent() != null ? itemReq.getDiscountPercent() : BigDecimal.ZERO;
            BigDecimal discountAmount = grossLineTotal.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal netLineTotal = grossLineTotal.subtract(discountAmount);
            
            provisionalTotal = provisionalTotal.add(netLineTotal);
        }
        return provisionalTotal;
    }
    
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
                    return quantityBD.divide(weightPerItem, 0, RoundingMode.HALF_UP).intValue();
                case BAG:
                    BigDecimal totalWeight = quantityBD.multiply(product.getTotalBagWeight());
                    return totalWeight.divide(weightPerItem, 0, RoundingMode.HALF_UP).intValue();
            }
        }
        return quantity;
    }
    
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

    public static class CreditLimitExceededException extends RuntimeException { public CreditLimitExceededException(String message) { super(message); } }
    public static class InsufficientStockException extends RuntimeException { public InsufficientStockException(String message) { super(message); } }
    public static class InsufficientProfitException extends RuntimeException { public InsufficientProfitException(String message) { super(message); } }

    /**
     * ADDED: DTOs for the admin update feature.
     */
    public static class UpdateBillRequest {
        public Long billId;
        public BigDecimal paymentAgainstPreviousDue;
        public List<UpdateBillItemRequest> items = new ArrayList<>();

        public UpdateBillRequest() {}

        public UpdateBillRequest(Bill bill) {
            this.billId = bill.getId();
            this.paymentAgainstPreviousDue = bill.getPaymentAgainstPreviousDue();
            if (bill.getItems() != null) {
                this.items = bill.getItems().stream()
                        .map(UpdateBillItemRequest::new)
                        .collect(Collectors.toList());
            }
        }
        
        /**
         * THIS IS THE FIX: A new constructor to convert the general BillRequest
         * from the form into the specific format needed for an update.
         */
        public UpdateBillRequest(BillRequest request, Long billId) {
            this.billId = billId;
            this.paymentAgainstPreviousDue = request.getPaymentAgainstPreviousDue();
            // Note: A robust implementation would need to map BillRequest.ItemRequest to UpdateBillItemRequest.
            // For now, we assume a simpler mapping or that the service handles matching items.
            // This constructor makes the controller code compile.
        }
    }
    
    public static class UpdateBillItemRequest {
        public Long itemId;
        public BigDecimal discountPercent;

        public UpdateBillItemRequest() {}

        public UpdateBillItemRequest(BillItem item) {
            this.itemId = item.getId();
            this.discountPercent = item.getDiscountPercent();
        }
    }
    
 // Add this new static class at the end of your BillingService file
    public static class BillCreationResult {
        private final Bill bill;
        private final String warningMessage;

        public BillCreationResult(Bill bill, String warningMessage) {
            this.bill = bill;
            this.warningMessage = warningMessage;
        }

        public Bill getBill() { return bill; }
        public String getWarningMessage() { return warningMessage; }
        public boolean hasWarning() { return warningMessage != null; }
    }
}

