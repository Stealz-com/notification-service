package com.ecommerce.notification.kafka;

import com.ecommerce.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    // Removed handleOrderNotification to avoid conflict with NotificationListener
    // which handles the detailed OrderPlacedEvent.

    @KafkaListener(topics = "userRegistrationTopic")
    public void handleRegistration(String message) {
        log.info("Received Registration Notification - {}", message);
        // Basic JSON parsing manually for simplicity or use Jackson
        String email = extractValue(message, "email");
        String username = extractValue(message, "username");
        if (email != null && username != null) {
            emailService.sendWelcomeEmail(email, username);
        }
    }

    private String extractValue(String json, String key) {
        try {
            // Very basic string manipulation to extract value from "key": "value"
            int keyIndex = json.indexOf("\"" + key + "\":");
            if (keyIndex == -1)
                return null;
            int valueStart = json.indexOf("\"", keyIndex + key.length() + 3) + 1;
            int valueEnd = json.indexOf("\"", valueStart);
            return json.substring(valueStart, valueEnd);
        } catch (Exception e) {
            log.error("Error parsing JSON", e);
            return null;
        }
    }
}
