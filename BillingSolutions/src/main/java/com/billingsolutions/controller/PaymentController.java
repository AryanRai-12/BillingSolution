//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Business;
//import com.billingsolutions.model.User;
//import com.billingsolutions.repository.BillRepository;
//import com.billingsolutions.repository.UserRepository;
//import com.billingsolutions.service.PaymentService;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.math.BigDecimal;
//
//@Controller
//@RequestMapping("/payments")
//public class PaymentController {
//
//    private final PaymentService paymentService;
//    private final UserRepository userRepository;
//    // ADDED: BillRepository is needed again to fetch bills for the payment form.
//    private final BillRepository billRepository;
//
//    public PaymentController(PaymentService paymentService, UserRepository userRepository, BillRepository billRepository) {
//        this.paymentService = paymentService;
//        this.userRepository = userRepository;
//        this.billRepository = billRepository;
//    }
//
//    /**
//     * Helper method to get the Business of the currently authenticated user.
//     */
//    private Business getCurrentBusiness() {
//        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
//        return currentUser.getBusiness();
//    }
//
//    /**
//     * Displays a list of all payments for the current business.
//     */
//    @GetMapping
//    public String listPayments(Model model) {
//        model.addAttribute("payments", paymentService.findAllForCurrentBusiness());
//        return "payments/list";
//    }
//
//    /**
//     * ADDED: Shows the form to create a new, separate payment.
//     */
//    @GetMapping("/new")
//    public String showPaymentForm(Model model) {
//        // Provide a list of recent bills to the form's dropdown/search.
//        PageRequest pageRequest = PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "id"));
//        model.addAttribute("bills", billRepository.findByBusiness(getCurrentBusiness(), pageRequest).getContent());
//        return "payments/form";
//    }
//
//    /**
//     * ADDED: Processes the creation of a new payment from the separate form.
//     */
//    @PostMapping("/new")
//    public String createPayment(@RequestParam("billId") Long billId,
//                                @RequestParam("amount") BigDecimal amount,
//                                RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.createPayment(billId, amount);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully.");
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while recording the payment.");
//        }
//        return "redirect:/payments";
//    }
//
//    /**
//     * Handles the deletion of a payment.
//     */
//    @PostMapping("/{id}/delete")
//    public String deletePayment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.deletePayment(id);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully.");
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while deleting the payment.");
//        }
//        return "redirect:/payments";
//    }
//}
//


//
//
//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Bill;
//import com.billingsolutions.model.Business;
//import com.billingsolutions.model.User;
//import com.billingsolutions.repository.BillRepository;
//import com.billingsolutions.repository.UserRepository;
//import com.billingsolutions.service.BillPaymentDetailsDTO;
//import com.billingsolutions.service.BillingService;
//import com.billingsolutions.service.PaymentService;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/payments")
//public class PaymentController {
//
//    private final PaymentService paymentService;
//    private final UserRepository userRepository;
//    private final BillRepository billRepository;
//    private final BillingService billingService;
//
//    public PaymentController(PaymentService paymentService, UserRepository userRepository, BillRepository billRepository, BillingService billingService) {
//        this.paymentService = paymentService;
//        this.userRepository = userRepository;
//        this.billRepository = billRepository;
//        this.billingService = billingService;
//    }
//
//    /**
//     * Displays a list of all payments for the current business.
//     */
//    @GetMapping
//    public String listPayments(Model model) {
//        model.addAttribute("payments", paymentService.findAllForCurrentBusiness());
//        return "payments/list";
//    }
//
//    /**
//     * Shows the form to create a new payment, filtered by financial year.
//     */
//    @GetMapping("/new")
//    public String showPaymentForm(@RequestParam(name = "financialYear", required = false) String financialYear, Model model) {
//        Business currentBusiness = getCurrentBusiness();
//        String effectiveFinancialYear = Optional.ofNullable(financialYear).orElse(getCurrentFinancialYear());
//
//        List<Bill> bills = billRepository.findByBusinessAndFinancialYear(
//            currentBusiness, 
//            effectiveFinancialYear, 
//            Sort.by(Sort.Direction.DESC, "id")
//        );
//
//        model.addAttribute("bills", bills);
//        model.addAttribute("financialYears", generateFinancialYears());
//        model.addAttribute("selectedFinancialYear", effectiveFinancialYear);
//        
//        return "payments/form";
//    }
//
//    /**
//     * Securely provides payment details for a specific bill as JSON data.
//     */
//    @GetMapping("/bill-details/{id}")
//    @ResponseBody
//    public ResponseEntity<BillPaymentDetailsDTO> getBillPaymentDetails(@PathVariable("id") Long id) {
//        try {
//            Bill bill = billingService.getBillById(id);
//            return ResponseEntity.ok(new BillPaymentDetailsDTO(bill));
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    /**
//     * Processes the creation of a new payment.
//     */
//    @PostMapping("/new")
//    public String createPayment(@RequestParam("billId") Long billId,
//                                @RequestParam("amount") BigDecimal amount,
//                                RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.createPayment(billId, amount);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        }
//        return "redirect:/payments";
//    }
//
//    /**
//     * Handles the deletion of a payment.
//     */
//    @PostMapping("/{id}/delete")
//    public String deletePayment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.deletePayment(id);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        }
//        return "redirect:/payments";
//    }
//
//    // --- Helper Methods ---
//
//    private String getCurrentFinancialYear() {
//        LocalDate today = LocalDate.now();
//        int year = today.getYear();
//        return (today.getMonthValue() < 4) ? (year - 1) + "-" + year : year + "-" + (year + 1);
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
//    private Business getCurrentBusiness() {
//        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
//        return currentUser.getBusiness();
//    }
//}
//


//
//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Bill;
//import com.billingsolutions.model.Business;
//import com.billingsolutions.model.User;
//import com.billingsolutions.repository.BillRepository;
//import com.billingsolutions.repository.UserRepository;
//import com.billingsolutions.service.BillPaymentDetailsDTO;
//import com.billingsolutions.service.BillingService;
//import com.billingsolutions.service.PaymentService;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/payments")
//public class PaymentController {
//
//    private final PaymentService paymentService;
//    private final UserRepository userRepository;
//    private final BillRepository billRepository;
//    // ADDED: The BillingService is needed to securely fetch bill details.
//    private final BillingService billingService;
//
//    public PaymentController(PaymentService paymentService, UserRepository userRepository, BillRepository billRepository, BillingService billingService) {
//        this.paymentService = paymentService;
//        this.userRepository = userRepository;
//        this.billRepository = billRepository;
//        this.billingService = billingService;
//    }
//
//    // ... (listPayments, deletePayment, and helper methods remain the same) ...
//
//    @GetMapping("/new")
//    public String showPaymentForm(@RequestParam(name = "financialYear", required = false) String financialYear, Model model) {
//        Business currentBusiness = getCurrentBusiness();
//        String effectiveFinancialYear = Optional.ofNullable(financialYear).orElse(getCurrentFinancialYear());
//
//        List<Bill> bills = billRepository.findByBusinessAndFinancialYear(
//            currentBusiness, 
//            effectiveFinancialYear, 
//            Sort.by(Sort.Direction.DESC, "id")
//        );
//
//        model.addAttribute("bills", bills);
//        model.addAttribute("financialYears", generateFinancialYears());
//        model.addAttribute("selectedFinancialYear", effectiveFinancialYear);
//        
//        return "payments/form";
//    }
//
//    /**
//     * MODIFIED: This endpoint now uses the updated DTO to provide a full payment summary.
//     * This will be called by JavaScript from the payment form.
//     */
//    @GetMapping("/bill-details/{id}")
//    @ResponseBody
//    public ResponseEntity<BillPaymentDetailsDTO> getBillPaymentDetails(@PathVariable("id") Long id) {
//        try {
//            Bill bill = billingService.getBillById(id);
//            BillPaymentDetailsDTO dto = new BillPaymentDetailsDTO(bill);
//            return ResponseEntity.ok(dto);
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//    
//    @GetMapping
//    public String listPayments(Model model) {
//        model.addAttribute("payments", paymentService.findAllForCurrentBusiness());
//        return "payments/list";
//    }
//
//    @PostMapping("/new")
//    public String createPayment(@RequestParam("billId") Long billId,
//                                @RequestParam("amount") BigDecimal amount,
//                                RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.createPayment(billId, amount);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        }
//        return "redirect:/payments";
//    }
//
//    @PostMapping("/{id}/delete")
//    public String deletePayment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.deletePayment(id);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        }
//        return "redirect:/payments";
//    }
//    
//    // --- Helper Methods ---
//
//    private String getCurrentFinancialYear() {
//        LocalDate today = LocalDate.now();
//        int year = today.getYear();
//        return (today.getMonthValue() < 4) ? (year - 1) + "-" + year : year + "-" + (year + 1);
//    }
//
//    private List<String> generateFinancialYears() {
//        List<String> years = new ArrayList<>();
//        LocalDate today = LocalDate.now();
//        int currentYear = today.getYear();
//        int endYear = (today.getMonthValue() < 4) ? currentYear : currentYear + 1;
//        for (int i = 0; i < 10; i++) {
//            int yearEnd = endYear - i;
//            int yearStart = yearEnd - 1;
//            years.add(yearStart + "-" + yearEnd);
//        }
//        return years;
//    }
//    
//    private Business getCurrentBusiness() {
//        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
//        return currentUser.getBusiness();
//    }
//}
//
//
//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Bill;
//import com.billingsolutions.model.Business;
//import com.billingsolutions.model.User;
//import com.billingsolutions.repository.BillRepository;
//import com.billingsolutions.repository.UserRepository;
//import com.billingsolutions.service.BillPaymentDetailsDTO;
//import com.billingsolutions.service.BillSummaryDTO;
//import com.billingsolutions.service.BillingService;
//import com.billingsolutions.service.PaymentService;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/payments")
//public class PaymentController {
//
//    private final PaymentService paymentService;
//    private final UserRepository userRepository;
//    private final BillRepository billRepository;
//    private final BillingService billingService;
//
//    public PaymentController(PaymentService paymentService, UserRepository userRepository, BillRepository billRepository, BillingService billingService) {
//        this.paymentService = paymentService;
//        this.userRepository = userRepository;
//        this.billRepository = billRepository;
//        this.billingService = billingService;
//    }
//
//    @GetMapping
//    public String listPayments(Model model) {
//        model.addAttribute("payments", paymentService.findAllForCurrentBusiness());
//        return "payments/list";
//    }
//
//    @GetMapping("/new")
//    public String showPaymentForm(@RequestParam(name = "financialYear", required = false) String financialYear, Model model) {
//        Business currentBusiness = getCurrentBusiness();
//        String effectiveFinancialYear = Optional.ofNullable(financialYear).orElse(getCurrentFinancialYear());
//
//        List<Bill> bills = billRepository.findByBusinessAndFinancialYear(
//            currentBusiness, 
//            effectiveFinancialYear, 
//            Sort.by(Sort.Direction.DESC, "id")
//        );
//
//        model.addAttribute("bills", bills);
//        model.addAttribute("financialYears", generateFinancialYears());
//        model.addAttribute("selectedFinancialYear", effectiveFinancialYear);
//        
//        return "payments/form";
//    }
//
//    @GetMapping("/bill-details/{id}")
//    @ResponseBody
//    public ResponseEntity<BillPaymentDetailsDTO> getBillPaymentDetails(@PathVariable("id") Long id) {
//        try {
//            Bill bill = billingService.getBillById(id);
//            return ResponseEntity.ok(new BillPaymentDetailsDTO(bill));
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//    
//    /**
//     * ADDED: A new endpoint specifically for the live bill search on the payment form.
//     * It returns a list of simple bill summary objects as JSON.
//     */
//    @GetMapping("/search-bills")
//    @ResponseBody
//    public ResponseEntity<List<BillSummaryDTO>> searchBillsForForm(
//            @RequestParam("financialYear") String financialYear,
//            @RequestParam("query") String query) {
//        
//        Business currentBusiness = getCurrentBusiness();
//        List<Bill> bills = billRepository.searchForPaymentForm(currentBusiness, financialYear, query);
//        
//        // We reuse the BillSummaryDTO as it contains all the necessary info for the dropdown
//        List<BillSummaryDTO> dtos = bills.stream()
//                                         .map(BillSummaryDTO::new)
//                                         .collect(Collectors.toList());
//                                         
//        return ResponseEntity.ok(dtos);
//    }
//
//    @PostMapping("/new")
//    public String createPayment(@RequestParam("billId") Long billId,
//                                @RequestParam("amount") BigDecimal amount,
//                                RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.createPayment(billId, amount);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        }
//        return "redirect:/payments";
//    }
//
//    @PostMapping("/{id}/delete")
//    public String deletePayment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//        try {
//            paymentService.deletePayment(id);
//            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
//        }
//        return "redirect:/payments";
//    }
//
//    // --- Helper Methods ---
//
//    private String getCurrentFinancialYear() {
//        LocalDate today = LocalDate.now();
//        int year = today.getYear();
//        return (today.getMonthValue() < 4) ? (year - 1) + "-" + year : year + "-" + (year + 1);
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
//    private Business getCurrentBusiness() {
//        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
//        return currentUser.getBusiness();
//    }
//}



package com.billingsolutions.controller;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.Business;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.BillRepository;
import com.billingsolutions.repository.UserRepository;
import com.billingsolutions.service.BillPaymentDetailsDTO;
import com.billingsolutions.service.BillSummaryDTO;
import com.billingsolutions.service.BillingService;
import com.billingsolutions.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final BillingService billingService;

    public PaymentController(PaymentService paymentService, UserRepository userRepository, BillRepository billRepository, BillingService billingService) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.billRepository = billRepository;
        this.billingService = billingService;
    }

    /**
     * Displays a list of all payments for the current business.
     */
    @GetMapping
    public String listPayments(Model model) {
        model.addAttribute("payments", paymentService.findAllForCurrentBusiness());
        return "payments/list";
    }

    /**
     * Shows the form to create a new payment, filtered by financial year.
     */
    @GetMapping("/new")
    public String showPaymentForm(@RequestParam(name = "financialYear", required = false) String financialYear, Model model) {
        Business currentBusiness = getCurrentBusiness();
        String effectiveFinancialYear = Optional.ofNullable(financialYear).orElse(getCurrentFinancialYear());

        List<Bill> bills = billRepository.findByBusinessAndFinancialYear(
            currentBusiness, 
            effectiveFinancialYear, 
            Sort.by(Sort.Direction.DESC, "id")
        );

        model.addAttribute("bills", bills); // Note: This is for the initial (now hidden) list.
        model.addAttribute("financialYears", generateFinancialYears());
        model.addAttribute("selectedFinancialYear", effectiveFinancialYear);
        
        return "payments/form";
    }

    /**
     * Securely provides payment details for a specific bill as JSON data.
     */
    @GetMapping("/bill-details/{id}")
    @ResponseBody
    public ResponseEntity<BillPaymentDetailsDTO> getBillPaymentDetails(@PathVariable("id") Long id) {
        try {
            Bill bill = billingService.getBillById(id);
            return ResponseEntity.ok(new BillPaymentDetailsDTO(bill));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * A new endpoint specifically for the live bill search on the payment form.
     * It returns a list of simple bill summary objects as JSON.
     */
    @GetMapping("/search-bills")
    @ResponseBody
    public ResponseEntity<List<BillSummaryDTO>> searchBillsForForm(
            @RequestParam("financialYear") String financialYear,
            @RequestParam("query") String query) {
        
        Business currentBusiness = getCurrentBusiness();
        List<Bill> bills = billRepository.searchForPaymentForm(currentBusiness, financialYear, query);
        
        // Use the BillingService to check the editable status of each bill
        List<BillSummaryDTO> dtos = bills.stream()
                                         .map(bill -> new BillSummaryDTO(bill, billingService.isBillActionable(bill)))
                                         .collect(Collectors.toList());
                                         
        return ResponseEntity.ok(dtos);
    }

    /**
     * Processes the creation of a new payment.
     */
    @PostMapping("/new")
    public String createPayment(@RequestParam("billId") Long billId,
                                @RequestParam("amount") BigDecimal amount,
                                RedirectAttributes redirectAttributes) {
        try {
            paymentService.createPayment(billId, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/payments";
    }

    /**
     * Handles the deletion of a payment.
     */
    @PostMapping("/{id}/delete")
    public String deletePayment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.deletePayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/payments";
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
    
    private Business getCurrentBusiness() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
        return currentUser.getBusiness();
    }
}

