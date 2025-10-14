//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Customer;
//import com.billingsolutions.service.CustomerService;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.validation.Valid;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//@RequestMapping("/customers")
//public class CustomerController {
//	private final CustomerService customerService;
//
//	public CustomerController(CustomerService customerService) {
//		this.customerService = customerService;
//	}
//  
//	// This is now secure because customerService.findAll() only returns customers for the current business.
//	@GetMapping
//	public String list(Model model) {
//		model.addAttribute("customers", customerService.findAll());
//		return "customers/list";
//	}
//
//	@GetMapping("/new")
//	public String createForm(Model model) {
//		model.addAttribute("customer", new Customer());
//		return "customers/form";
//	}
//
//	// This is secure because customerService.save() automatically assigns the new customer to the current business.
//	@PostMapping
//	public String create(@Valid @ModelAttribute("customer") Customer customer, BindingResult result, RedirectAttributes redirectAttributes) {
//		if (result.hasErrors()) return "customers/form";
//		customerService.save(customer);
//		redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully!");
//		return "redirect:/customers";
//	}
//
//	// MODIFIED: Updated to handle the new return type of findById and catch exceptions.
//	@GetMapping("/{id}/edit")
//	public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//		try {
//			Customer customer = customerService.findById(id); // This now returns Customer directly or throws exception.
//			model.addAttribute("customer", customer);
//			return "customers/form";
//		} catch (EntityNotFoundException e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
//			return "redirect:/customers";
//		}
//	}
//
//	// MODIFIED: Updated to securely find the customer before updating.
//	@PostMapping("/{id}")
//	public String update(@PathVariable("id") Long id,
//	                     @Valid @ModelAttribute("customer") Customer form,
//	                     BindingResult result, RedirectAttributes redirectAttributes) {
//	    if (result.hasErrors()) return "customers/form";
//	    
//		try {
//			Customer customer = customerService.findById(id); // Securely finds the customer first.
//
//			// Update all fields from the form object.
//			customer.setName(form.getName());
//			customer.setAddress(form.getAddress());
//			customer.setPhone(form.getPhone());
//			customer.setGst(form.getGst());
//			customer.setEmail(form.getEmail());
//			customer.setCreditLimit(form.getCreditLimit());
//			customer.setLandmark(form.getLandmark());
//			customer.setHouseAddress(form.getHouseAddress());
//			customer.setPartyCode(form.getPartyCode());
//			customer.setDue(form.getDue());
//			customer.setCounterName(form.getCounterName());
//			customer.setCustomerGroup(form.getCustomerGroup());
//			
//			customerService.save(customer);
//			redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully!");
//			return "redirect:/customers";
//		} catch (EntityNotFoundException e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
//			return "redirect:/customers";
//		}
//	}
//	
//	// MODIFIED: Added error handling for cases where the customer might not exist.
//	@PostMapping("/{id}/delete")
//    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//        try {
//			customerService.deleteById(id);
//			redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully!");
//		} catch (EntityNotFoundException e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
//		}
//        return "redirect:/customers";
//    }
//}
package com.billingsolutions.controller;

import com.billingsolutions.model.Customer;
import com.billingsolutions.service.CustomerGroupService; // Make sure this is imported
import com.billingsolutions.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {
	private final CustomerService customerService;
	private final CustomerGroupService customerGroupService; // Inject this service

	public CustomerController(CustomerService customerService, CustomerGroupService customerGroupService) {
		this.customerService = customerService;
		this.customerGroupService = customerGroupService;
	}
  
	@GetMapping
	public String list(Model model) {
		model.addAttribute("customers", customerService.findAll());
		return "customers/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("customer", new Customer());
		model.addAttribute("allCustomerGroups", customerGroupService.findAllForCurrentBusiness());
		return "customers/form";
	}

	@PostMapping
	public String create(@Valid @ModelAttribute("customer") Customer customer, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("allCustomerGroups", customerGroupService.findAllForCurrentBusiness());
			return "customers/form";
		}
		customerService.save(customer);
		redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully!");
		return "redirect:/customers";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Customer customer = customerService.findById(id);
			model.addAttribute("customer", customer);
			model.addAttribute("allCustomerGroups", customerGroupService.findAllForCurrentBusiness());
			return "customers/form";
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
			return "redirect:/customers";
		}
	}

	// === THIS METHOD IS NOW FIXED AND CLEANED UP ===
	@PostMapping("/{id}")
	public String update(@PathVariable("id") Long id,
	                     @Valid @ModelAttribute("customer") Customer form,
	                     BindingResult result, Model model, RedirectAttributes redirectAttributes) {
	    if (result.hasErrors()) {
	        // FIX 1: Reload the group list when validation fails
	        model.addAttribute("allCustomerGroups", customerGroupService.findAllForCurrentBusiness());
	        return "customers/form";
	    }
	    
		try {
			// FIX 2: Delegate the update logic to the service
			customerService.update(id, form); 
			redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully!");
			return "redirect:/customers";
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
			return "redirect:/customers";
		}
	}
	
	@PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
			customerService.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully!");
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
		}
        return "redirect:/customers";
    }
	
}