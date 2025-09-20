package com.billingsolutions.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	private final UserDetailsService userDetailsService;

	public SecurityConfig(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/billing/**", "/customers/**", "/vendors/**", "/products/**", "/").hasAnyRole("USER", "ADMIN")
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/", true)
				.permitAll()
			)
			.logout(Customizer.withDefaults())
			.userDetailsService(userDetailsService);
		return http.build();
	}
} 