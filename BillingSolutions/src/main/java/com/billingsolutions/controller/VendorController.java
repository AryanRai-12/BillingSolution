package com.billingsolutions.controller;

import com.billingsolutions.model.Vendor;
import com.billingsolutions.service.VendorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendors")
public class VendorController {
	private final VendorService vendorService;

	public VendorController(VendorService vendorService) {
		this.vendorService = vendorService;
	}

	// This is now secure because vendorService.findAll() only returns vendors for the current business.
	@GetMapping
	public String list(Model model) {
		model.addAttribute("vendors", vendorService.findAll());
		return "vendors/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("vendor", new Vendor());
		return "vendors/form";
	}

	// This is secure because vendorService.save() automatically assigns the new vendor to the current business.
	@PostMapping
	public String create(@Valid @ModelAttribute("vendor") Vendor vendor, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) return "vendors/form";
		vendorService.save(vendor);
		redirectAttributes.addFlashAttribute("successMessage", "Vendor created successfully!");
		return "redirect:/vendors";
	}

	// MODIFIED: Updated to handle the new return type of findById and catch exceptions.
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Vendor vendor = vendorService.findById(id); // This now returns Vendor directly or throws exception.
			model.addAttribute("vendor", vendor);
			return "vendors/form";
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
			return "redirect:/vendors";
		}
	}

	// MODIFIED: Updated to securely find the vendor before updating.
	@PostMapping("/{id}")
	public String update(@PathVariable("id") Long id,
	                     @Valid @ModelAttribute("vendor") Vendor form,
	                     BindingResult result, RedirectAttributes redirectAttributes) {
	    if (result.hasErrors()) return "vendors/form";
	    
		try {
			Vendor vendor = vendorService.findById(id); // Securely finds the vendor first.

			// Update all fields from the form object.
			vendor.setName(form.getName());
			vendor.setAddress(form.getAddress());
			vendor.setPhone(form.getPhone());
			vendor.setGst(form.getGst());
			vendor.setEmail(form.getEmail());
			vendor.setCreditLimit(form.getCreditLimit());
			vendor.setLandmark(form.getLandmark());
			vendor.setHouseAddress(form.getHouseAddress());
			vendor.setPartyCode(form.getPartyCode());
			vendor.setDue(form.getDue());
			vendor.setCompanyName(form.getCompanyName());
			vendor.setVendorGroup(form.getVendorGroup());
			
			vendorService.save(vendor);
			redirectAttributes.addFlashAttribute("successMessage", "Vendor updated successfully!");
			return "redirect:/vendors";
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
			return "redirect:/vendors";
		}
	}
	
	// MODIFIED: Added error handling for cases where the vendor might not exist.
	@PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
			vendorService.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Vendor deleted successfully!");
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
		}
        return "redirect:/vendors";
    }
}
