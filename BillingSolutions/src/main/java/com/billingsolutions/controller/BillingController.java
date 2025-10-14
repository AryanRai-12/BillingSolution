//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Bill;
//import com.billingsolutions.repository.CustomerRepository;
//import com.billingsolutions.repository.ProductRepository;
//import com.billingsolutions.repository.UserRepository;
//import com.billingsolutions.service.BillRequest;
//import com.billingsolutions.service.BillingService;
//import com.billingsolutions.service.BillingService.CreditLimitExceededException;
//import com.billingsolutions.service.BillingService.InsufficientStockException;
//import com.billingsolutions.service.InsufficientProfitException;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//@Controller
//@RequestMapping("/billing")
//public class BillingController {
//    private final BillingService billingService;
//    private final ProductRepository productRepository;
//    private final CustomerRepository customerRepository;
//    private final UserRepository userRepository;
//
//    public BillingController(BillingService billingService,
//                             ProductRepository productRepository,
//                             CustomerRepository customerRepository,
//                             UserRepository userRepository) {
//        this.billingService = billingService;
//        this.productRepository = productRepository;
//        this.customerRepository = customerRepository;
//        this.userRepository = userRepository;
//    }
//    
//    @ExceptionHandler({
//        CreditLimitExceededException.class,
//        InsufficientStockException.class,
//        InsufficientProfitException.class,
//        IllegalArgumentException.class 
//    })
//    public String handleBillingException(RuntimeException ex, RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("error", ex.getMessage());
//        return "redirect:/billing/create";
//    }
//    
//    
//    @GetMapping
//    public String listAndSearchBills(@RequestParam(name = "query", required = false) String query,
//                                     @RequestParam(name = "page", defaultValue = "0") int page,
//                                     Model model) {
//        Pageable pageable = PageRequest.of(page, 15, Sort.by("id").descending());
//        Page<Bill> billsPage;
//
//        if (query != null && !query.isBlank()) {
//            billsPage = billingService.searchBills(query, pageable);
//            if (billsPage.isEmpty()) {
//                model.addAttribute("notFoundMessage", "No bills found matching your search for '" + query + "'.");
//            }
//        } else {
//            billsPage = billingService.findAllForCurrentBusiness(pageable);
//        }
//
//        model.addAttribute("bills", billsPage.getContent());
//        // Add pagination attributes to the model later if you want to add page numbers to the UI
//        
//        return "billing/list";
//    }
//    
//    
//    @GetMapping("/create")
//    public String showCreateBillForm(Model model) {
//        // THE FIX: Securely get the current business first.
//        com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();
//
//        // THE FIX: These calls now use the secure, business-aware repository methods.
//        model.addAttribute("products", productRepository.findAllWithVendorsByBusiness(currentBusiness));
//        model.addAttribute("customers", customerRepository.findByBusiness(currentBusiness));
//        
//        model.addAttribute("salesmen", userRepository.findByBusinessAndRolesContaining(currentBusiness, "ROLE_SALESMAN"));
//        model.addAttribute("financialYears", generateFinancialYears());
//
//        if (!model.containsAttribute("request")) {
//            BillRequest request = new BillRequest();
//            request.setFinancialYear(getCurrentFinancialYear());
//            model.addAttribute("request", request);
//        }
//        return "billing/form";
//    }
//
//    @PostMapping("/create")
//    public String createBill(@ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
//        try {
//            Bill createdBill = billingService.createBill(request);
//            redirectAttributes.addFlashAttribute("successMessage", "Bill #" + createdBill.getBillNo() + " created successfully for FY " + createdBill.getFinancialYear());
//            return "redirect:/billing/" + createdBill.getId();
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
//            redirectAttributes.addFlashAttribute("request", request);
//            return "redirect:/billing/create";
//        }
//    }
//
//    @GetMapping("/{id}")
//    public String viewBill(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            Bill bill = billingService.getBillById(id);
//            model.addAttribute("bill", bill);
//            return "billing/view";
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
//            return "redirect:/"; 
//        }
//    }
//
//    // --- Helper Methods ---
//    
//    private String getCurrentFinancialYear() {
//        LocalDate today = LocalDate.now();
//        int year = today.getYear();
//        if (today.getMonthValue() < 4) {
//            return (year - 1) + "-" + year;
//        } else {
//            return year + "-" + (year + 1);
//        }
//    }
//
//    private List<String> generateFinancialYears() {
//        List<String> years = new ArrayList<>();
//        LocalDate today = LocalDate.now();
//        int currentYear = today.getYear();
//        int endYear = (today.getMonthValue() < 4) ? currentYear : currentYear + 1;
//
//        for (int i = 0; i < 10; i++) {
//            int yearEnd = endYear - i;
//            int yearStart = yearEnd - 1;
//            years.add(yearStart + "-" + yearEnd);
//        }
//        return years;
//    }
//
//    /**
//     * Helper method to get the Business of the currently authenticated user.
//     * This is the core of the multi-tenancy security.
//     */
//    private com.billingsolutions.model.Business getCurrentBusiness() {
//        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        com.billingsolutions.model.User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
//        return currentUser.getBusiness();
//    }
//}
//
package com.billingsolutions.controller;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.Customer;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.BillRepository;
import com.billingsolutions.repository.CustomerRepository;
import com.billingsolutions.repository.ProductRepository;
import com.billingsolutions.repository.UserRepository;
import com.billingsolutions.service.BillRequest;
import com.billingsolutions.service.BillSummaryDTO;
import com.billingsolutions.service.BillingService;
import com.billingsolutions.service.BillingService.CreditLimitExceededException;
import com.billingsolutions.service.BillingService.InsufficientStockException;
import com.billingsolutions.service.BillingService.UpdateBillRequest;
import com.billingsolutions.service.CustomerBillDueDTO;
import com.billingsolutions.service.CustomerDto;
import com.billingsolutions.service.CustomerGroupService;
import com.billingsolutions.service.CustomerService;
import com.billingsolutions.service.InsufficientProfitException;
import com.billingsolutions.service.ProductDTO;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Controller
@RequestMapping("/billing")
public class BillingController {
    private final BillingService billingService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final CustomerGroupService customerGroupService; // Add this field


    public BillingController(BillingService billingService,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             UserRepository userRepository,
                             CustomerService customerService, 
                             BillRepository billRepository,
                             CustomerGroupService customerGroupService) {
        this.billingService = billingService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.customerService = customerService;
        this.billRepository = billRepository;
        this.customerGroupService = customerGroupService;
    }
    
    
    
    
    @GetMapping("/customer-dues/{customerId}")
    @ResponseBody
    public ResponseEntity<List<CustomerBillDueDTO>> getCustomerDues(@PathVariable("customerId") Long customerId) {
        try {
            // Securely find the customer to ensure they belong to the current business.
            Customer customer = customerService.findById(customerId);
            // Use the new repository method to find only their unpaid bills.
            List<Bill> unpaidBills = billRepository.findUnpaidBillsByCustomer(customer);
            // Convert the results into simple DTOs to send to the webpage.
            List<CustomerBillDueDTO> dtos = unpaidBills.stream()
                                                    .map(CustomerBillDueDTO::new)
                                                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (EntityNotFoundException e) {
            // If the customer ID is invalid or doesn't belong to the business, return an error.
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Handles the initial display of the bill history page and processes any submitted filters.
     */
    @GetMapping
    public String listAndSearchBills(@RequestParam(name = "query", required = false) String query,
                                     @RequestParam(name = "financialYear") Optional<String> financialYearOpt,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     Model model) {
        
        String effectiveFinancialYear = financialYearOpt.orElse(getCurrentFinancialYear());
        Pageable pageable = PageRequest.of(page, 15, Sort.by("id").descending());
        Page<BillSummaryDTO> billsPage = billingService.searchBills(query, effectiveFinancialYear, pageable);

        model.addAttribute("bills", billsPage.getContent());
        model.addAttribute("query", query);
        model.addAttribute("selectedFinancialYear", effectiveFinancialYear);
        model.addAttribute("financialYears", generateFinancialYears());
        
        if (billsPage.isEmpty() && (query != null && !query.isBlank() || !effectiveFinancialYear.isBlank())) {
            model.addAttribute("notFoundMessage", "No bills found matching your criteria.");
        }
        
        return "billing/list";
    }

    /**
     * MODIFIED: This endpoint now returns a list of BillSummaryDTOs as JSON.
     * This is the final step that fixes the live search bug.
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<BillSummaryDTO>> searchBillRows(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "financialYear", required = false) String financialYear) {
                
        Pageable pageable = PageRequest.of(0, 100, Sort.by("id").descending());
        Page<BillSummaryDTO> billsPage = billingService.searchBills(query, financialYear, pageable);
        return ResponseEntity.ok(billsPage.getContent());
    }
    
    /**
     * Displays the form to create a new bill.
     */
//    @GetMapping("/create")
//    public String showCreateBillForm(Model model) {
//        com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();
//        model.addAttribute("products", productRepository.findAllWithVendorsByBusiness(currentBusiness));
//        model.addAttribute("customers", customerRepository.findByBusiness(currentBusiness));
//        model.addAttribute("salesmen", userRepository.findByBusinessAndRolesContaining(currentBusiness, "ROLE_SALESMAN"));
//        model.addAttribute("financialYears", generateFinancialYears());
//
//        if (!model.containsAttribute("request")) {
//            BillRequest request = new BillRequest();
//            request.setFinancialYear(getCurrentFinancialYear());
//            model.addAttribute("request", request);
//        }
//        return "billing/form";
//    }
    
    //hulllaaaa
    
    @GetMapping("/create")
    public String showCreateBillForm(Model model) {
        com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();

        // **THE FIX**: Convert Product entities to a safe List of ProductDTOs
        List<ProductDTO> productDTOs = productRepository.findAllWithVendorsByBusiness(currentBusiness)
                .stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        model.addAttribute("products", productDTOs); // Pass the safe DTO list
        
        //model.addAttribute("bill", new Bill());
        List<Customer> customers = customerRepository.findByBusiness(currentBusiness);
        List<CustomerDto> customerDtos = customers.stream()
                .map(customer -> new CustomerDto(
                    customer.getId(),
                    customer.getName(),
                    // Safely get the group ID, handling nulls
                    (customer.getCustomerGroup() != null) ? customer.getCustomerGroup().getId() : null
                ))
                .collect(Collectors.toList());
        
        
        model.addAttribute("customers", customerDtos);
        
        model.addAttribute("salesmen", userRepository.findByBusinessAndRolesContaining(currentBusiness, "ROLE_SALESMAN"));
        model.addAttribute("financialYears", generateFinancialYears());
        model.addAttribute("allCustomerGroups", customerGroupService.findAllForCurrentBusiness());
        
        if (!model.containsAttribute("request")) {
            BillRequest request = new BillRequest();
            request.setFinancialYear(getCurrentFinancialYear());
            model.addAttribute("request", request);
        }
        return "billing/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditBillForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Bill bill = billingService.getBillById(id);
            //model.addAttribute("bill", bill);
            if (!billingService.isBillActionable(bill)) {
                redirectAttributes.addFlashAttribute("errorMessage", "This bill can no longer be edited.");
                return "redirect:/billing";
            }
            
            if (!model.containsAttribute("request")) {
                model.addAttribute("request", new BillRequest(bill));
            }
            
            com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();

            // **THE FIX**: Convert Product entities to a safe List of ProductDTOs here too
            List<ProductDTO> productDTOs = productRepository.findAllWithVendorsByBusiness(currentBusiness)
                    .stream()
                    .map(ProductDTO::new)
                    .collect(Collectors.toList());
            model.addAttribute("products", productDTOs); // Pass the safe DTO list
            
            List<Customer> customers = customerRepository.findByBusiness(currentBusiness);
            List<CustomerDto> customerDtos = customers.stream()
            	    .map(c -> new CustomerDto(
            	        c.getId(),
            	        c.getName(),
            	        // This safely gets the group ID while the database session is still open
            	        (c.getCustomerGroup() != null) ? c.getCustomerGroup().getId() : null
            	    ))
            	    .collect(Collectors.toList());
            
            model.addAttribute("allCustomerGroups", customerGroupService.findAllForCurrentBusiness());

            model.addAttribute("customers", customerDtos);
            model.addAttribute("salesmen", userRepository.findByBusinessAndRolesContaining(currentBusiness, "ROLE_SALESMAN"));
            model.addAttribute("financialYears", generateFinancialYears());
            
            return "billing/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
            return "redirect:/billing";
        }
    }

//    @PostMapping("/{id}/edit")
//    public String processBillUpdate(@PathVariable("id") Long id, @ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
//         try {
//            billingService.updateBill(id, request);
//            redirectAttributes.addFlashAttribute("successMessage", "Bill updated successfully!");
//            return "redirect:/billing/" + id;
//        } catch (Exception e) {
//            // When an error happens, we redirect back to the edit form.
//            // The user's input is passed along in 'request'.
//            redirectAttributes.addFlashAttribute("request", request); 
//            redirectAttributes.addFlashAttribute("errorMessage", "Error updating bill: " + e.getMessage());
//            return "redirect:/billing/" + id + "/edit";
//        }
//    }
    
    @GetMapping("/billing/form")
    public String showForm(Model model) {
        model.addAttribute("billRequest", new BillRequest());
        return "billing/form";
    }
    @PostMapping("/{id}/edit")
    public String processBillUpdate(@PathVariable("id") Long id, @ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
         try {
            billingService.updateBill(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Bill updated successfully!");
            return "redirect:/billing/" + id;
        } catch (Exception e) {
            // ======================== THIS IS THE CRITICAL FIX ========================
            // This code cleans the list, removing any nulls before they crash the template.
            if (request.getItems() != null) {
                request.getItems().removeIf(Objects::isNull);
            }
            // ==========================================================================

            redirectAttributes.addFlashAttribute("request", request); 
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating bill: " + e.getMessage());
            return "redirect:/billing/" + id + "/edit";
        }
    }
    //hulllaaa 

    /**
     * Processes the creation of a new bill.
     */
//    @PostMapping("/create")
//    public String createBill(@ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
//        try {
//            Bill createdBill = billingService.createBill(request);
//            redirectAttributes.addFlashAttribute("successMessage", "Bill #" + createdBill.getBillNo() + " created successfully for FY " + createdBill.getFinancialYear());
//            return "redirect:/billing/" + createdBill.getId();
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
//            redirectAttributes.addFlashAttribute("request", request);
//            return "redirect:/billing/create";
//        }
//    }
    @PostMapping("/create")
    public String createBill(@ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
        try {
            // The service now returns our new result object
            BillingService.BillCreationResult result = billingService.createBill(request);
            Bill createdBill = result.getBill();

            // Check if the result has a warning message
            if (result.hasWarning()) {
                redirectAttributes.addFlashAttribute("warningMessage", result.getWarningMessage());
            }

            redirectAttributes.addFlashAttribute("successMessage", "Bill #" + createdBill.getBillNo() + " created successfully for FY " + createdBill.getFinancialYear());
            return "redirect:/billing/" + createdBill.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/billing/create";
        }
    }
    @GetMapping("/{id}")
    public String viewBill(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Bill bill = billingService.getBillById(id);
            model.addAttribute("bill", bill);

            // THIS IS THE CRUCIAL LINE YOU ARE MISSING
            // It checks if the bill can be edited and tells the HTML page.
            model.addAttribute("isEditable", billingService.isBillActionable(bill)); 
            
            return "billing/view";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
            return "redirect:/billing";
        }
    }
    /**
     * Securely displays the details of a single bill.
     */
//    @GetMapping("/{id}")
//    public String viewBill(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            Bill bill = billingService.getBillById(id);
//            model.addAttribute("bill", bill);
//            return "billing/view";
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
//            return "redirect:/billing";
//        }
//    }
    
//    /**
//     * MODIFIED: This method now prepares the data for the edit form.
//     * It fetches the bill, converts it to a BillRequest object, and then reuses the main form template.
//     */
//    @GetMapping("/{id}/edit")
//    public String showEditBillForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            Bill bill = billingService.getBillById(id);
//            if (!billingService.isBillActionable(bill)) {
//                redirectAttributes.addFlashAttribute("errorMessage", "This bill can no longer be edited.");
//                return "redirect:/billing";
//            }
//            
//            // Convert the Bill entity into a BillRequest DTO to pre-populate the form
//            BillRequest request = new BillRequest(bill);
//            model.addAttribute("request", request);
//            
//            // Add other necessary data for the form's dropdowns
//            com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();
//            model.addAttribute("products", productRepository.findAllWithVendorsByBusiness(currentBusiness));
//            model.addAttribute("customers", customerRepository.findByBusiness(currentBusiness));
//            model.addAttribute("salesmen", userRepository.findByBusinessAndRolesContaining(currentBusiness, "ROLE_SALESMAN"));
//            model.addAttribute("financialYears", generateFinancialYears());
//            
//            return "billing/form"; // Reuse the main form template
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
//            return "redirect:/billing";
//        }
//    }
//
//    /**
//     * MODIFIED: This method now processes the submission from the unified form.
//     */
//    @PostMapping("/{id}/edit")
//    public String processEditBill(@PathVariable("id") Long id, @ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
//         try {
//            Bill bill = billingService.getBillById(id);
//            if (!billingService.isBillActionable(bill)) {
//                redirectAttributes.addFlashAttribute("errorMessage", "This bill can no longer be edited.");
//                return "redirect:/billing";
//            }
//            
//            // The service method needs an UpdateBillRequest, so we convert it here.
//            UpdateBillRequest updateRequest = new UpdateBillRequest(request, id);
//            billingService.adminUpdateBill(updateRequest);
//
//            redirectAttributes.addFlashAttribute("successMessage", "Bill updated successfully!");
//            return "redirect:/billing/" + id;
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
//            return "redirect:/billing";
//        }
//    }
    
    /**
     * MODIFIED: This method now prepares the data for the edit form
     * and reuses the main "billing/form" template.
     */
    /**
     * ADDED: Displays the form for editing a bill, reusing the main form template.
     */
//    @GetMapping("/{id}/edit")
//    public String showEditBillForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            Bill bill = billingService.getBillById(id);
//            if (!billingService.isBillActionable(bill)) {
//                redirectAttributes.addFlashAttribute("errorMessage", "This bill can no longer be edited.");
//                return "redirect:/billing";
//            }
//            
//            model.addAttribute("request", new BillRequest(bill));
//            
//            com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();
//            model.addAttribute("products", productRepository.findAllWithVendorsByBusiness(currentBusiness));
//            model.addAttribute("customers", customerRepository.findByBusiness(currentBusiness));
//            model.addAttribute("salesmen", userRepository.findByBusinessAndRolesContaining(currentBusiness, "ROLE_SALESMAN"));
//            model.addAttribute("financialYears", generateFinancialYears());
//            
//            return "billing/form";
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
//            return "redirect:/billing";
//        }
//    }

    /**
     * ADDED: Processes the submission of the updated bill from the unified form.
     */
//    @PostMapping("/{id}/edit")
//    public String processBillUpdate(@PathVariable("id") Long id, @ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
//         try {
//            billingService.updateBill(id, request);
//            redirectAttributes.addFlashAttribute("successMessage", "Bill updated successfully!");
//            return "redirect:/billing/" + id;
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error updating bill: " + e.getMessage());
//            return "redirect:/billing/" + id + "/edit";
//        }
//    }
    
    /**
     * ADDED: Handles the deletion of a bill.
     */
    @PostMapping("/{id}/delete")
    public String deleteBill(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            billingService.deleteBill(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bill deleted successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
        } catch (SecurityException e) {
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting bill.");
        }
        return "redirect:/billing";
    }
    
    
    @ExceptionHandler({
        CreditLimitExceededException.class,
        InsufficientStockException.class,
        InsufficientProfitException.class,
        IllegalArgumentException.class 
    })
    public String handleBillingException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/billing/create";
    }

    // --- Helper Methods ---
    
    private String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        return (today.getMonthValue() < 4) ? (year - 1) + "-" + year : year + "-" + (year + 1);
    }

    private List<String> generateFinancialYears() {
        List<String> years = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int endYear = (today.getMonthValue() < 4) ? currentYear : currentYear + 1;
        for (int i = 0; i < 10; i++) {
            int yearEnd = endYear - i;
            int yearStart = yearEnd - 1;
            years.add(yearStart + "-" + yearEnd);
        }
        return years;
    }

    private com.billingsolutions.model.Business getCurrentBusiness() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
        return currentUser.getBusiness();
    }
}

