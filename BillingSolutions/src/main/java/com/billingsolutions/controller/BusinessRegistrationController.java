package com.billingsolutions.controller;

import com.billingsolutions.service.BusinessRegistrationRequest;
import com.billingsolutions.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register-business")
public class BusinessRegistrationController {

    private final BusinessService businessService;

    public BusinessRegistrationController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /**
     * Displays the business registration form.
     * @param model The Spring model to add attributes to.
     * @return The path to the registration form template.
     */
    @GetMapping
    public String showRegistrationForm(Model model) {
        // Add an empty request object to the model to bind the form fields.
        model.addAttribute("registrationRequest", new BusinessRegistrationRequest());
        return "auth/register-business"; // Path to your new HTML form
    }

    /**
     * Processes the submission of the business registration form.
     * @param request The DTO with the submitted form data, validated.
     * @param result The result of the validation.
     * @param redirectAttributes Used to pass messages after a redirect.
     * @return A redirect path to the login page on success, or back to the form on error.
     */
    @PostMapping
    public String processRegistration(@Valid @ModelAttribute("registrationRequest") BusinessRegistrationRequest request,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {

        // If validation fails (e.g., empty fields, invalid email), return to the form.
        if (result.hasErrors()) {
            return "auth/register-business";
        }

        try {
            businessService.registerNewBusiness(request);
            // If successful, add a success message and redirect to the login page.
            redirectAttributes.addFlashAttribute("successMessage", "Business registered successfully! Please log in.");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            // If the business service throws an error (e.g., email already exists),
            // add the error to the binding result and return to the form.
            result.rejectValue("ownerEmail", "error.registrationRequest", e.getMessage());
            return "auth/register-business";
        }
    }
}
