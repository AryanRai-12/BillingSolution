package com.billingsolutions.controller;

import com.billingsolutions.model.User;
import com.billingsolutions.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping
	public String listUsers(Model model) {
		List<User> users = userService.findAll();
		model.addAttribute("users", users);
		return "admin/users/list";
	}

	@GetMapping("/new")
	public String createUserForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("availableRoles", Arrays.asList("USER", "ADMIN"));
		return "admin/users/form";
	}

	@PostMapping
	public String createUser(@ModelAttribute User user, @RequestParam("roles") String[] roles) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRoles(new HashSet<>(Arrays.asList(roles)));
		userService.save(user);
		return "redirect:/admin/users";
	}

	@GetMapping("/{id}/edit")
	public String editUserForm(@PathVariable("id") Long id, Model model) {
		User user = userService.findById(id).orElseThrow();
		model.addAttribute("user", user);
		model.addAttribute("availableRoles", Arrays.asList("USER", "ADMIN"));
		return "admin/users/form";
	}

	@PostMapping("/{id}")
	public String updateUser(@PathVariable("id") Long id, @ModelAttribute User form, @RequestParam("roles") String[] roles) {
		User user = userService.findById(id).orElseThrow();
		user.setUsername(form.getUsername());
		if (form.getPassword() != null && !form.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(form.getPassword()));
		}
		user.setRoles(new HashSet<>(Arrays.asList(roles)));
		user.setEnabled(form.isEnabled());
		userService.save(user);
		return "redirect:/admin/users";
	}

	@PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id) {
		userService.deleteById(id);
		return "redirect:/admin/users";
	}
} 