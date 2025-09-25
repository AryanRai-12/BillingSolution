package com.billingsolutions.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@billingsolutions.com");
        message.setTo(to);
        message.setSubject("Your Admin Registration OTP");
        message.setText("Your One-Time Password is: " + otp);
        mailSender.send(message);
    }
}
