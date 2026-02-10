package com.ecommerce.notification.listener;

import com.ecommerce.notification.dto.OrderPlacedEvent;
import com.ecommerce.notification.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "notificationTopic", groupId = "notificationId")
    public void handleNotification(String message) {
        log.info("Received notification message: {}", message);
        try {
            // Check if message is JSON or just plain string (legacy support)
            if (message.startsWith("{")) {
                OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
                if (event.getEmail() != null) {
                    // 1. Send Email
                    emailService.sendOrderConfirmationEmail(event);
                    log.info("Sent order confirmation email to {}", event.getEmail());

                    // 2. Send Real-time Notification
                    // Assuming we can map email or some ID to a user. For now broadcasting to a
                    // user-specific topic if we had userId,
                    // or just a general topic. Let's try to send to /topic/orders for demo, or
                    // better /user/{email}/queue/notifications

                    // Sending to a specific user queue (if they are subscribed)
                    // We use the email as the user identifier for simplicity in this demo context
                    messagingTemplate.convertAndSendToUser(event.getEmail(), "/queue/notifications",
                            "Order Placed Successfully! Order #: " + event.getOrderNumber());

                    // Also sending to a public topic for testing if user-specific fail
                    messagingTemplate.convertAndSend("/topic/orders", "New Order: " + event.getOrderNumber());

                } else {
                    log.warn("Order event received but no email present: {}", event.getOrderNumber());
                }
            } else {
                // Legacy: message is orderNumber. But we don't have email in this case.
                log.warn(
                        "Received legacy string message (Order Number): {}. Cannot send email without recipient address.",
                        message);
                // We could implement a fallback fetch from User Service here if we had userId,
                // but for now we rely on the new JSON event.
            }
        } catch (Exception e) {
            log.error("Error processing notification message: {}", message, e);
        }
    }

    @KafkaListener(topics = "orderStatusTopic", groupId = "notificationId")
    public void handleOrderStatusUpdate(String message) {
        log.info("Received order status update: {}", message);
        try {
            com.ecommerce.notification.dto.OrderStatusUpdateEvent event = objectMapper.readValue(message,
                    com.ecommerce.notification.dto.OrderStatusUpdateEvent.class);
            if (event.getUserId() != null) {
                // Relay to user via WebSocket
                messagingTemplate.convertAndSendToUser(
                        event.getUserId(),
                        "/queue/order-updates",
                        event);
                log.info("Relayed status update for order {} to user {}", event.getOrderId(), event.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing order status update: {}", message, e);
        }
    }
}
