package com.ecommerce.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationListener {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationListener.class);

    @Autowired
    private JavaMailSender mailSender;

    @KafkaListener(topics = "emailVerificationTopic", groupId = "notificationId")
    public void handleEmailVerification(String message) {
        // Message format: email,token,username,role
        String[] parts = message.split(",");
        if (parts.length >= 4) {
            String email = parts[0];
            String token = parts[1];
            String username = parts[2];
            String role = parts[3];

            String verificationLink = "http://localhost:3000/verify-email?token=" + token + "&email=" + email
                    + "&usertype=" + role;

            log.info("==================================================");
            log.info("EMAIL VERIFICATION REQUEST");
            log.info("To: {}", email);
            log.info("Username: {}", username);
            log.info("VERIFICATION LINK: {}", verificationLink);
            log.info("==================================================");

            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(email);
                mailMessage.setSubject("Verify your email for Ecommerce");
                mailMessage.setText("Hello " + username + ",\n\n" +
                        "Please click the link below to verify your email:\n" +
                        verificationLink + "\n\n" +
                        "This link will expire in 30 minutes.\n\n" +
                        "Best regards,\nEcommerce Team");
                mailMessage.setFrom("aniket9766228627@gmail.com");

                mailSender.send(mailMessage);
                log.info("Verification email sent successfully to: {}", email);
            } catch (Exception e) {
                log.error("Failed to send verification email to {}: {}", email, e.getMessage());
            }
        } else {
            log.warn("Received malformed email verification message: {}", message);
        }
    }
}
