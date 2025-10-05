//package com.billingsolutions.controller;
//
////import com.billingsolutions.model.User;
////import com.billingsolutions.service.EmailService;
////import com.billingsolutions.service.UserService;
////import jakarta.servlet.http.HttpSession;
////import org.springframework.stereotype.Controller;
////import org.springframework.ui.Model;
////import org.springframework.web.bind.annotation.GetMapping;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestMapping;
////import org.springframework.web.bind.annotation.RequestParam;
////import org.springframework.web.servlet.mvc.support.RedirectAttributes;
////
////import java.util.Random;
////
////@Controller
////@RequestMapping("/register-admin")
////public class AdminRegistrationController {
////	private final EmailService emailService;
////    private final UserService userService;
////
////    public AdminRegistrationController(EmailService emailService, UserService userService) {
////        this.emailService = emailService;
////        this.userService = userService;
////    }
////
////    @GetMapping
////    public String showRegistrationForm(Model model, RedirectAttributes redirectAttributes) {
////        if (userService.hasAdminAccount()) {
////            redirectAttributes.addFlashAttribute("error", "Admin registration is closed. An admin account already exists.");
////            return "redirect:/login";
////        }
////        model.addAttribute("user", new User());
////        return "admin/register";
////    }
////
////    @PostMapping("/start")
////    public String startRegistration(User user, HttpSession session, RedirectAttributes redirectAttributes) {
////        if (userService.hasAdminAccount()) {
////            redirectAttributes.addFlashAttribute("error", "Admin registration is closed.");
////            return "redirect:/login";
////        }
////
////        String otp = String.format("%06d", new Random().nextInt(999999));
////        emailService.sendOtpEmail(user.getEmail(), otp);
////
////        session.setAttribute("registrationUser", user);
////        session.setAttribute("otp", otp);
////
////        return "redirect:/register-admin/verify";
////    }
////
////    @GetMapping("/verify")
////    public String showOtpVerificationForm(HttpSession session, RedirectAttributes redirectAttributes) {
////        if (session.getAttribute("registrationUser") == null) {
////            redirectAttributes.addFlashAttribute("error", "Your session has expired. Please start over.");
////            return "redirect:/register-admin";
////        }
////        return "admin/verify-otp";
////    }
////
////    @PostMapping("/complete")
////    public String completeRegistration(@RequestParam String otp, HttpSession session, Model model) {
////        String sessionOtp = (String) session.getAttribute("otp");
////        User user = (User) session.getAttribute("registrationUser");
////
////        if (sessionOtp == null || user == null) {
////            model.addAttribute("error", "Your session has expired. Please register again.");
////            return "admin/register";
////        }
////
////        if (otp.equals(sessionOtp)) {
////            userService.registerAdmin(user);
////
////            session.removeAttribute("otp");
////            session.removeAttribute("registrationUser");
////
////            return "redirect:/login?adminRegistered";
////        } else {
////            model.addAttribute("error", "Invalid OTP. Please try again.");
////            return "admin/verify-otp";
////        }
////    }
////}
////import com.billingsolutions.model.User;
////import com.billingsolutions.service.EmailService;
////import com.billingsolutions.service.UserService;
////import jakarta.servlet.http.HttpSession;
////import org.springframework.stereotype.Controller;
////import org.springframework.ui.Model;
////import org.springframework.web.bind.annotation.GetMapping;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestMapping;
////import org.springframework.web.bind.annotation.RequestParam;
////import org.springframework.web.servlet.mvc.support.RedirectAttributes;
////
////import java.util.Random;
////
////@Controller
////@RequestMapping("/register-admin")
////public class AdminRegistrationController {
////	private final EmailService emailService;
////    private final UserService userService;
////
////    public AdminRegistrationController(EmailService emailService, UserService userService) {
////        this.emailService = emailService;
////        this.userService = userService;
////    }
////
////    @GetMapping
////    public String showRegistrationForm(Model model, RedirectAttributes redirectAttributes) {
////        if (userService.hasAdminAccount()) {
////            redirectAttributes.addFlashAttribute("error", "Admin registration is closed. An admin account already exists.");
////            return "redirect:/login";
////        }
////        model.addAttribute("user", new User());
////        return "admin/register";
////    }
////
////    @PostMapping("/start")
////    public String startRegistration(User user, HttpSession session, RedirectAttributes redirectAttributes) {
////        if (userService.hasAdminAccount()) {
////            redirectAttributes.addFlashAttribute("error", "Admin registration is closed.");
////            return "redirect:/login";
////        }
////
////        // --- DEBUG LOGGING STEP 1 ---
////        // Check what's in the User object immediately after form submission.
////        System.out.println("--- Step 1: In startRegistration ---");
////        System.out.println("Received User from form. Username: " + user.getUsername());
////        System.out.println("Password is present: " + (user.getPassword() != null && !user.getPassword().isEmpty()));
////        // -----------------------------
////
////        String otp = String.format("%06d", new Random().nextInt(999999));
////        emailService.sendOtpEmail(user.getEmail(), otp);
////
////        session.setAttribute("registrationUser", user);
////        session.setAttribute("otp", otp);
////
////        System.out.println("User object stored in session.");
////        return "redirect:/register-admin/verify";
////    }
////
////    @GetMapping("/verify")
////    public String showOtpVerificationForm(HttpSession session, RedirectAttributes redirectAttributes) {
////        if (session.getAttribute("registrationUser") == null) {
////            redirectAttributes.addFlashAttribute("error", "Your session has expired. Please start over.");
////            return "redirect:/register-admin";
////        }
////        return "admin/verify-otp";
////    }
////
////    @PostMapping("/complete")
////    public String completeRegistration(@RequestParam String otp, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
////        String sessionOtp = (String) session.getAttribute("otp");
////        User user = (User) session.getAttribute("registrationUser");
////
////        if (sessionOtp == null || user == null) {
////            redirectAttributes.addFlashAttribute("error", "Your session has expired. Please register again.");
////            return "redirect:/register-admin";
////        }
////        
////        // --- DEBUG LOGGING STEP 2 ---
////        // Check the User object after retrieving it from the session.
////        System.out.println("--- Step 2: In completeRegistration ---");
////        System.out.println("Retrieved User from session. Username: " + user.getUsername());
////        System.out.println("Password is present: " + (user.getPassword() != null && !user.getPassword().isEmpty()));
////        // -----------------------------
////
////        if (otp.equals(sessionOtp)) {
////            try {
////                userService.registerAdmin(user);
////
////                session.removeAttribute("otp");
////                session.removeAttribute("registrationUser");
////                
////                redirectAttributes.addFlashAttribute("success", "Admin registered successfully! Please log in.");
////                return "redirect:/login";
////            } catch (Exception e) {
////                // If any error happens in the service layer, show it.
////                model.addAttribute("error", "Could not register admin. " + e.getMessage());
////                return "admin/verify-otp";
////            }
////        } else {
////            model.addAttribute("error", "Invalid OTP. Please try again.");
////            return "admin/verify-otp";
////        }
////    }
////}
////
//import com.billingsolutions.model.User;
//import com.billingsolutions.service.EmailService;
//import com.billingsolutions.service.UserService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.Random;
//
//@Controller
//@RequestMapping("/register-admin")
//public class AdminRegistrationController {
//	private final EmailService emailService;
//    private final UserService userService;
//
//    public AdminRegistrationController(EmailService emailService, UserService userService) {
//        this.emailService = emailService;
//        this.userService = userService;
//    }
//
//    @GetMapping
//    public String showRegistrationForm(Model model, RedirectAttributes redirectAttributes) {
//        if (userService.hasAdminAccount()) {
//            redirectAttributes.addFlashAttribute("error", "Admin registration is closed. An admin account already exists.");
//            return "redirect:/login";
//        }
//        model.addAttribute("user", new User());
//        return "admin/register";
//    }
//
//    @PostMapping("/start")
//    public String startRegistration(User user, HttpSession session, RedirectAttributes redirectAttributes) {
//        if (userService.hasAdminAccount()) {
//            redirectAttributes.addFlashAttribute("error", "Admin registration is closed.");
//            return "redirect:/login";
//        }
//
//        // --- DEBUG LOGGING STEP 1 ---
//        System.out.println("--- Step 1: In startRegistration ---");
//        System.out.println("Received User from form. Username: " + user.getUsername());
//        System.out.println("Password is present: " + (user.getPassword() != null && !user.getPassword().isEmpty()));
//        // -----------------------------
//
//        String otp = String.format("%06d", new Random().nextInt(999999));
//        emailService.sendOtpEmail(user.getEmail(), otp);
//
//        session.setAttribute("registrationUser", user);
//        session.setAttribute("otp", otp);
//
//        System.out.println("User object stored in session.");
//        return "redirect:/register-admin/verify";
//    }
//
//    @GetMapping("/verify")
//    public String showOtpVerificationForm(HttpSession session, RedirectAttributes redirectAttributes) {
//        if (session.getAttribute("registrationUser") == null) {
//            redirectAttributes.addFlashAttribute("error", "Your session has expired. Please start over.");
//            return "redirect:/register-admin";
//        }
//        return "admin/verify-otp";
//    }
//
//    @PostMapping("/complete")
//    // CORRECTED: Explicitly name the request parameter to bind to the 'otp' variable.
//    public String completeRegistration(@RequestParam("otp") String otp, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
//        String sessionOtp = (String) session.getAttribute("otp");
//        User user = (User) session.getAttribute("registrationUser");
//
//        if (sessionOtp == null || user == null) {
//            redirectAttributes.addFlashAttribute("error", "Your session has expired. Please register again.");
//            return "redirect:/register-admin";
//        }
//        
//        // --- DEBUG LOGGING STEP 2 ---
//        System.out.println("--- Step 2: In completeRegistration ---");
//        System.out.println("Retrieved User from session. Username: " + user.getUsername());
//        System.out.println("Password is present: " + (user.getPassword() != null && !user.getPassword().isEmpty()));
//        // -----------------------------
//
//        if (otp.equals(sessionOtp)) {
//            try {
//                userService.registerAdmin(user);
//
//                session.removeAttribute("otp");
//                session.removeAttribute("registrationUser");
//                
//                redirectAttributes.addFlashAttribute("success", "Admin registered successfully! Please log in.");
//                return "redirect:/login";
//            } catch (Exception e) {
//                // If any error happens in the service layer, show it.
//                model.addAttribute("error", "Could not register admin. " + e.getMessage());
//                return "admin/verify-otp";
//            }
//        } else {
//            model.addAttribute("error", "Invalid OTP. Please try again.");
//            return "admin/verify-otp";
//        }
//    }
//}
//
