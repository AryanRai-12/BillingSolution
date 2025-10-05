package com.billingsolutions.controller;

import com.billingsolutions.model.User;
import com.billingsolutions.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class UserController {
	
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	// This is now secure because userService.findAll() only returns users for the current business.
	@GetMapping
	public String listUsers(Model model) {
		model.addAttribute("users", userService.findAll());
		return "admin/users/list";
	}

	@GetMapping("/new")
	public String showCreateUserForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("availableRoles", List.of("USER", "ADMIN", "SALESMAN"));
		return "admin/users/form";
	}

	// This is secure because userService.save() automatically assigns the new user to the current business.
	@PostMapping
	public String processUserCreation(@ModelAttribute User user, @RequestParam(name = "roles", required = false) Set<String> roles, RedirectAttributes attrs) {
		user.setRoles(processRoles(roles, true));
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userService.save(user);

		attrs.addFlashAttribute("successMessage", "User created successfully!");
		return "redirect:/admin/users";
	}

	// This is secure because userService.findById() will return empty if the user doesn't belong to this business.
	@GetMapping("/{id}/edit")
	public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
		return userService.findById(id)
			.map(user -> {
				model.addAttribute("user", user);
				model.addAttribute("availableRoles", List.of("USER", "ADMIN", "SALESMAN"));
				return "admin/users/form";
			})
			.orElseGet(() -> {
				redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
				return "redirect:/admin/users";
			});
	}

	// This is secure because findById confirms ownership before the save operation.
	@PostMapping("/{id}")
	public String processUserUpdate(@PathVariable("id") Long id, @ModelAttribute("user") User userFormData, @RequestParam(name = "roles", required = false) Set<String> roles, RedirectAttributes attrs) {
		try {
			User existingUser = userService.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
			
			existingUser.setUsername(userFormData.getUsername());
			existingUser.setEmail(userFormData.getEmail());
			existingUser.setEnabled(userFormData.isEnabled());

			if (userFormData.getPassword() != null && !userFormData.getPassword().isEmpty()) {
				existingUser.setPassword(passwordEncoder.encode(userFormData.getPassword()));
			}
			
			existingUser.setRoles(processRoles(roles, false));
			
			userService.save(existingUser);
			attrs.addFlashAttribute("successMessage", "User updated successfully!");
			return "redirect:/admin/users";
		} catch (EntityNotFoundException e) {
			attrs.addFlashAttribute("errorMessage", "User not found.");
			return "redirect:/admin/users";
		}
	}

	// This is secure because userService.deleteById() verifies ownership before deleting.
	@PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes attrs) {
		try {
			userService.deleteById(id);
			attrs.addFlashAttribute("successMessage", "User deleted successfully!");
		} catch (EntityNotFoundException e) {
			attrs.addFlashAttribute("errorMessage", "User not found.");
		}
		return "redirect:/admin/users";
	}

	/**
	 * Helper method to process roles from the form, ensuring the "ROLE_" prefix is added.
	 */
	private Set<String> processRoles(Set<String> roles, boolean defaultToUser) {
		if (roles == null || roles.isEmpty()) {
			return defaultToUser ? Set.of("ROLE_USER") : new HashSet<>();
		}
		return roles.stream()
					.map(role -> "ROLE_" + role.toUpperCase())
					.collect(Collectors.toSet());
	}
}

