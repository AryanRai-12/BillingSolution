package com.billingsolutions.controller;

import com.billingsolutions.model.Bill;
import com.billingsolutions.repository.CustomerRepository;
import com.billingsolutions.repository.ProductRepository;
import com.billingsolutions.repository.SalesmanRepository;
import com.billingsolutions.repository.UserRepository;
import com.billingsolutions.service.BillRequest;
import com.billingsolutions.service.BillingService;
import com.billingsolutions.service.InsufficientProfitException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/billing")
public class BillingController {
    private final BillingService billingService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public BillingController(BillingService billingService,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             UserRepository userRepository) {
        this.billingService = billingService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;;
    }
    
    @ExceptionHandler({
        BillingService.CreditLimitExceededException.class,
        BillingService.InsufficientStockException.class,
        BillingService.InsufficientProfitException.class,
        IllegalArgumentException.class 
    })
    public String handleBillingException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        // Add the exception's message as a flash attribute to show on the next page
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        
        // Redirect the user back to the create bill form
        return "redirect:/billing/create";
    }

    

    @GetMapping("/create")
    public String showCreateBillForm(Model model) {
//        model.addAttribute("products", productRepository.findAll());
    	model.addAttribute("products", productRepository.findAllWithVendors());
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("salesmen", userRepository.findByRolesContaining("ROLE_SALESMAN"));
        
        model.addAttribute("financialYears", generateFinancialYears());
        
        if (!model.containsAttribute("request")) {
            BillRequest request = new BillRequest();
            // THE FIX: Set the default financial year to the current one when the form is first loaded.
            request.setFinancialYear(getCurrentFinancialYear());
            model.addAttribute("request", request);
        }
//        if (!model.containsAttribute("request")) {
//            model.addAttribute("request", new BillRequest());
//        }
        return "billing/form";
    }
    
    private String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        // Indian financial year starts in April.
        if (today.getMonthValue() < 4) {
            return (year - 1) + "-" + year;
        } else {
            return year + "-" + (year + 1);
        }
    }
    
    private List<String> generateFinancialYears() {
        List<String> years = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        
        // Determine the end year of the current financial year
        int endYear = (today.getMonthValue() < 4) ? currentYear : currentYear + 1;

        for (int i = 0; i < 10; i++) {
            int yearEnd = endYear - i;
            int yearStart = yearEnd - 1;
            years.add(yearStart + "-" + yearEnd);
        }
        return years;
    }

    @PostMapping("/create")
    public String createBill(@ModelAttribute("request") BillRequest request, RedirectAttributes redirectAttributes) {
        try {
            Bill createdBill = billingService.createBill(request);
            redirectAttributes.addFlashAttribute("successMessage", "Bill #" + createdBill.getId() + " created successfully!");
            return "redirect:/billing/" + createdBill.getId();
        } catch (InsufficientProfitException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/billing/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/billing/create";
        }
    }

    @GetMapping("/{id}")
    public String viewBill(@PathVariable("id") Long id, Model model) {
        Bill bill = billingService.getBillById(id);
        model.addAttribute("bill", bill);
        return "billing/view";
    }
}