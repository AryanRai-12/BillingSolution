package com.billingsolutions.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

//@Component
//public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        
//        String redirectUrl = determineTargetUrl(authentication);
//
//        if (response.isCommitted()) {
//            System.out.println("Response has already been committed. Unable to redirect to " + redirectUrl);
//            return;
//        }
//
//        response.sendRedirect(redirectUrl);
//    }
//
//    protected String determineTargetUrl(final Authentication authentication) {
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//
//        for (GrantedAuthority grantedAuthority : authorities) {
//            if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
//                return "/admin/dashboard";
//            }
//        }
//        
//        // If not admin, redirect to the user's home page.
//        return "/";
//    }
//}
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // THE FIX: Always redirect every user to the main dashboard ("/") after login.
        response.sendRedirect(request.getContextPath() + "/");
    }
}