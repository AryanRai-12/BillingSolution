package com.billingsolutions.controller;

import com.billingsolutions.model.CustomerGroup;
import com.billingsolutions.service.CustomerGroupService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer-groups")
public class CustomerGroupController {

    private final CustomerGroupService customerGroupService;

    @Autowired
    public CustomerGroupController(CustomerGroupService customerGroupService) {
        this.customerGroupService = customerGroupService;
    }

    @GetMapping
    public String list(Model model) {
        if (!model.containsAttribute("customerGroup")) {
            model.addAttribute("customerGroup", new CustomerGroup());
        }
        model.addAttribute("groups", customerGroupService.findAllForCurrentBusiness());
        // This is correct: it returns the path to your HTML file.
        return "customergroup/form"; 
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customerGroup") CustomerGroup customerGroup,
                         BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.customerGroup", result);
            redirectAttributes.addFlashAttribute("customerGroup", customerGroup);
            // FIX: Redirect to the GET mapping URL
            return "redirect:/customer-groups";
        }
        
        try {
            customerGroupService.save(customerGroup);
            redirectAttributes.addFlashAttribute("successMessage", "Group created successfully!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: A group with this name already exists for your business.");
            redirectAttributes.addFlashAttribute("customerGroup", customerGroup);
        }
        // FIX: Always redirect after a POST action
        return "redirect:/customer-groups";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            customerGroupService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Group deleted successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group not found.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: This group is in use by a customer and cannot be deleted.");
        }
        // FIX: Redirect to the GET mapping URL
        return "redirect:/customer-groups";
    }

    // This API endpoint for the modal is correct and does not need changes.
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createFromModal(@RequestBody @Valid CustomerGroup group, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Group name cannot be empty.");
        }
        try {
            CustomerGroup savedGroup = customerGroupService.save(group);
            return ResponseEntity.ok(savedGroup);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("A group with this name already exists.");
        }
    }
    
    // The API endpoint for editing is also correct and needs no changes.
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateFromModal(@PathVariable("id") Long id, 
                                             @RequestBody @Valid CustomerGroup group, 
                                             BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Group name cannot be empty.");
        }
        try {
            CustomerGroup updatedGroup = customerGroupService.update(id, group);
            return ResponseEntity.ok(updatedGroup);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("A group with this name already exists.");
        }
    }
}