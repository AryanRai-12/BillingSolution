package com.billingsolutions.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {
	private final UserDetailsService userDetailsService;
	private final AuthenticationSuccessHandler customAuthenticationSuccessHandler;
	public SecurityConfig(UserDetailsService userDetailsService,AuthenticationSuccessHandler customAuthenticationSuccessHandler) {
		this.userDetailsService = userDetailsService;
		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;;
	}

//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		http
//			.authorizeHttpRequests(auth -> auth
//				.requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
//				.requestMatchers("/admin/**").hasRole("ADMIN")
//				.requestMatchers("/billing/**", "/customers/**", "/vendors/**", "/products/**", "/").hasAnyRole("USER", "ADMIN")
//				.anyRequest().authenticated()
//			)
//			.formLogin(form -> form
//				.loginPage("/login")
//				.defaultSuccessUrl("/", true)
//				.permitAll()
//			)
//			.logout(Customizer.withDefaults())
//			.userDetailsService(userDetailsService);
//		return http.build();
//	}
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				// ADD THIS LINE to allow public access to the registration flow.
					.requestMatchers("/register-admin/**", "/css/**", "/js/**", "/images/**", "/login").permitAll()
					
					// 2. Define access for the most specific routes first (admin area)
					.requestMatchers("/admin/**").hasRole("ADMIN")
					
					// 3. Define access for routes NOT available to SALESMAN
					.requestMatchers("/vendors/**").hasAnyRole("ADMIN", "USER")
					
					// 4. Define access for common routes shared by everyone
					.requestMatchers("/", "/billing/**", "/customers/**", "/products/**").hasAnyRole("ADMIN", "USER", "SALESMAN")
					
					// 5. All other requests must be authenticated (fallback)
					.anyRequest().authenticated()
				
			)
			.formLogin(form -> form
				.loginPage("/login")
				// UPDATE THIS LINE to use the custom handler for role-based redirects.
				.successHandler(customAuthenticationSuccessHandler)
				.permitAll()
			)
			.logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
			.userDetailsService(userDetailsService);
		return http.build();
	}
} 