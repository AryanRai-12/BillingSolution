package com.billingsolutions.controller;

import com.billingsolutions.model.User;
import com.billingsolutions.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class UserController {
//	private final UserService userService;
//	private final PasswordEncoder passwordEncoder;
//
//	public UserController(UserService userService, PasswordEncoder passwordEncoder) {
//		this.userService = userService;
//		this.passwordEncoder = passwordEncoder;
//	}
//
//	@GetMapping
//	public String listUsers(Model model) {
//		List<User> users = userService.findAll();
//		model.addAttribute("users", users);
//		return "admin/users/list";
//	}
//
//	@GetMapping("/new")
//	public String createUserForm(Model model) {
//		model.addAttribute("user", new User());
//		model.addAttribute("availableRoles", Arrays.asList("USER", "ADMIN"));
//		return "admin/users/form";
//	}
//
//	@PostMapping
//	public String createUser(@ModelAttribute User user, @RequestParam("roles") String[] roles) {
//		user.setPassword(passwordEncoder.encode(user.getPassword()));
//		user.setRoles(new HashSet<>(Arrays.asList(roles)));
//		userService.save(user);
//		return "redirect:/admin/users";
//	}
//
//	@GetMapping("/{id}/edit")
//	public String editUserForm(@PathVariable("id") Long id, Model model) {
//		User user = userService.findById(id).orElseThrow();
//		model.addAttribute("user", user);
//		model.addAttribute("availableRoles", Arrays.asList("USER", "ADMIN"));
//		return "admin/users/form";
//	}
//
//	@PostMapping("/{id}")
//	public String updateUser(@PathVariable("id") Long id, @ModelAttribute User form, @RequestParam("roles") String[] roles) {
//		User user = userService.findById(id).orElseThrow();
//		user.setUsername(form.getUsername());
//		if (form.getPassword() != null && !form.getPassword().isEmpty()) {
//			user.setPassword(passwordEncoder.encode(form.getPassword()));
//		}
//		user.setRoles(new HashSet<>(Arrays.asList(roles)));
//		user.setEnabled(form.isEnabled());
//		userService.save(user);
//		return "redirect:/admin/users";
//	}
//
//	@PostMapping("/{id}/delete")
//    public String deleteUser(@PathVariable("id") Long id) {
//		userService.deleteById(id);
//		return "redirect:/admin/users";
//	}
	
//	wohooo
	
//	private final UserService userService;
//	private final PasswordEncoder passwordEncoder;
//
//	public UserController(UserService userService, PasswordEncoder passwordEncoder) {
//		this.userService = userService;
//		this.passwordEncoder = passwordEncoder;
//	}
//
//	@GetMapping
//	public String listUsers(Model model) {
//		List<User> users = userService.findAll();
//		model.addAttribute("users", users);
//		return "admin/users/list"; // This correctly shows the list of users.
//	}
//
//	@GetMapping("/new")
//	public String createUserForm(Model model) {
//		model.addAttribute("user", new User());
//		model.addAttribute("availableRoles", Arrays.asList("USER", "ADMIN"));
//		// --- THIS IS THE FIX ---
//		// This must return the 'form' template, not the 'list' template.
//		return "admin/users/form";
//	}
//
//	@PostMapping
//	public String createUser(@ModelAttribute User user, @RequestParam(value = "roles", required = false) String[] roles, RedirectAttributes redirectAttributes) {
//		user.setPassword(passwordEncoder.encode(user.getPassword()));
//		user.setRoles(roles != null ? new HashSet<>(Arrays.asList(roles)) : new HashSet<>());
//		userService.save(user);
//		redirectAttributes.addFlashAttribute("success", "User created successfully!");
//		return "redirect:/admin/users";
//	}
//
//	@GetMapping("/{id}/edit")
//	public String editUserForm(@PathVariable("id") Long id, Model model) {
//		User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
//		model.addAttribute("user", user);
//		model.addAttribute("availableRoles", Arrays.asList("USER", "ADMIN", "ROLE_ADMIN")); // Include all possibilities
//		return "admin/users/form";
//	}
//
//	@PostMapping("/{id}")
//	public String updateUser(@PathVariable("id") Long id, @ModelAttribute User form, @RequestParam(value = "roles", required = false) String[] roles, RedirectAttributes redirectAttributes) {
//		User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
//		user.setUsername(form.getUsername());
//        user.setEmail(form.getEmail()); // Also update email
//		if (form.getPassword() != null && !form.getPassword().isEmpty()) {
//			user.setPassword(passwordEncoder.encode(form.getPassword()));
//		}
//		user.setRoles(roles != null ? new HashSet<>(Arrays.asList(roles)) : new HashSet<>());
//		user.setEnabled(form.isEnabled());
//		userService.save(user);
//		redirectAttributes.addFlashAttribute("success", "User updated successfully!");
//		return "redirect:/admin/users";
//	}
//
//	@PostMapping("/{id}/delete")
//    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//		userService.deleteById(id);
//		redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
//		return "redirect:/admin/users";
//	}
	
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping
	public String listUsers(Model model) {
		model.addAttribute("users", userService.findAll());
		return "admin/users/list";
	}

	@GetMapping("/new")
	public String showCreateUserForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("availableRoles", List.of("USER", "ADMIN","SALESMAN"));
		return "admin/users/form";
	}

	@PostMapping
	public String processUserCreation(@ModelAttribute User user, @RequestParam(name = "roles", required = false) Set<String> roles, RedirectAttributes attrs) {
		// THE FIX: We now fully prepare the user object here to prevent the service layer from overwriting it.
		// 1. Set the roles correctly using the helper method.
		user.setRoles(processRoles(roles, true));
		// 2. Encode the password directly in the controller.
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		// 3. Call the generic 'save' method, which is less likely to have conflicting logic.
		userService.save(user);

		attrs.addFlashAttribute("successMessage", "User created successfully!");
		return "redirect:/admin/users";
	}

	@GetMapping("/{id}/edit")
	public String showEditUserForm(@PathVariable("id") Long id, Model model) {
		User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
		model.addAttribute("user", user);
		model.addAttribute("availableRoles", List.of("USER", "ADMIN","SALESMAN"));
		return "admin/users/form";
	}

	@PostMapping("/{id}")
	public String processUserUpdate(@PathVariable("id") Long id, @ModelAttribute("user") User userFormData, @RequestParam(name = "roles", required = false) Set<String> roles, RedirectAttributes attrs) {
		User existingUser = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
		
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
	}

	@PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes attrs) {
		userService.deleteById(id);
		attrs.addFlashAttribute("successMessage", "User deleted successfully!");
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