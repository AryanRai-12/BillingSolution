package com.billingsolutions.controller;

import com.billingsolutions.model.Bill;
import com.billingsolutions.repository.CustomerRepository;
import com.billingsolutions.repository.ProductRepository;
import com.billingsolutions.repository.SalesmanRepository;
import com.billingsolutions.service.BillRequest;
import com.billingsolutions.service.BillingService;
import com.billingsolutions.service.InsufficientProfitException;
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
    private final SalesmanRepository salesmanRepository;

    public BillingController(BillingService billingService,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             SalesmanRepository salesmanRepository) {
        this.billingService = billingService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesmanRepository = salesmanRepository;
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
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("salesmen", salesmanRepository.findAll());
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new BillRequest());
        }
        return "billing/form";
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