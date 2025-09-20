package com.billingsolutions.service;

import com.billingsolutions.model.User;
import com.billingsolutions.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostConstruct
	@Transactional
	public void initializeDefaultUsers() {
		// Only create default users if no users exist
		if (userRepository.count() == 0) {
			// Create admin user
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("admin123"));
			admin.setRoles(new HashSet<>(Arrays.asList("ADMIN", "USER")));
			admin.setEnabled(true);
			userRepository.save(admin);

			// Create regular user
			User user = new User();
			user.setUsername("user");
			user.setPassword(passwordEncoder.encode("user123"));
			user.setRoles(new HashSet<>(Arrays.asList("USER")));
			user.setEnabled(true);
			userRepository.save(user);

			System.out.println("Default users created:");
			System.out.println("Admin: admin/admin123");
			System.out.println("User: user/user123");
		}
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Transactional
	public User save(User user) {
		return userRepository.save(user);
	}

	@Transactional
	public void deleteById(Long id) {
		userRepository.deleteById(id);
	}
} 