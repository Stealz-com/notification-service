package com.ecommerce.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendWelcomeEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        String htmlContent = templateEngine.process("welcome-email", context);
        sendEmail(to, "Welcome to Ecommerce", htmlContent);
    }

    public void sendOrderConfirmationEmail(String to, String orderNumber) {
        Context context = new Context();
        context.setVariable("orderNumber", orderNumber);
        String htmlContent = templateEngine.process("order-email", context);
        sendEmail(to, "Order Confirmation", htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
