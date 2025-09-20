package com.billingsolutions.controller;

import com.billingsolutions.model.Customer;
import com.billingsolutions.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerController {
	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}
  
	@GetMapping
	public String list(Model model) {
		model.addAttribute("customers", customerService.findAll());
		return "customers/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("customer", new Customer());
		return "customers/form";
	}

	@PostMapping
	public String create(@Valid @ModelAttribute("customer") Customer customer, BindingResult result) {
		if (result.hasErrors()) return "customers/form";
		customerService.save(customer);
		return "redirect:/customers";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Long id, Model model) {
		Customer customer = customerService.findById(id).orElseThrow();
		model.addAttribute("customer", customer);
		return "customers/form";
	}

	@PostMapping("/{id}")
	public String update(@PathVariable("id") Long id,
	                     @Valid @ModelAttribute("customer") Customer form,
	                     BindingResult result) {
	    if (result.hasErrors()) return "customers/form";
	    Customer customer = customerService.findById(id).orElseThrow();

	    customer.setName(form.getName());
	    customer.setAddress(form.getAddress());
	    customer.setPhone(form.getPhone());
	    customer.setGst(form.getGst());
	    customer.setEmail(form.getEmail());
	    customer.setCreditLimit(form.getCreditLimit());
	    customer.setLandmark(form.getLandmark());
	    customer.setHouseAddress(form.getHouseAddress());
	    customer.setPartyCode(form.getPartyCode());
	    customer.setDue(form.getDue());
	    customerService.save(customer);
	    return "redirect:/customers";
	}
	
	@PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        customerService.deleteById(id);
        return "redirect:/customers";
    }
} 