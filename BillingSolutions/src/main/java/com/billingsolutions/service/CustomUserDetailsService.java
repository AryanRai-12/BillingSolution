package com.billingsolutions.service;

import com.billingsolutions.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final UserService userService;

	public CustomUserDetailsService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userService.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getUsername())
			.password(user.getPassword())
			.authorities(user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
				.collect(Collectors.toList()))
			.disabled(!user.isEnabled())
			.build();
	}
} 