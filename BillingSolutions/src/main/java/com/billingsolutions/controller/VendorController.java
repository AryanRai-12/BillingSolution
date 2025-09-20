package com.billingsolutions.controller;

import com.billingsolutions.model.Vendor;
import com.billingsolutions.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vendors")
public class VendorController {
	private final VendorService vendorService;

	public VendorController(VendorService vendorService) {
		this.vendorService = vendorService;
	}

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

	@PostMapping
	public String create(@Valid @ModelAttribute("vendor") Vendor vendor, BindingResult result) {
		if (result.hasErrors()) return "vendors/form";
		vendorService.save(vendor);
		return "redirect:/vendors";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Long id, Model model) {
		Vendor vendor = vendorService.findById(id).orElseThrow();
		model.addAttribute("vendor", vendor);
		return "vendors/form";
	}

	@PostMapping("/{id}")
	public String update(@PathVariable("id") Long id,
	                     @Valid @ModelAttribute("vendor") Vendor form,
	                     BindingResult result) {
	    if (result.hasErrors()) return "vendors/form";
	    Vendor vendor = vendorService.findById(id).orElseThrow();

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
	    vendorService.save(vendor);
	    return "redirect:/vendors";
	}
	
	@PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        vendorService.deleteById(id);
        return "redirect:/vendors";
    }
} 