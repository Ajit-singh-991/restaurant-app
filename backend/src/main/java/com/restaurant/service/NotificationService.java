package com.restaurant.service;

import com.restaurant.dto.NotificationDto;
import com.restaurant.entity.Order;
import com.restaurant.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyOrderCreated(Order order) {
        NotificationDto notification = buildOrderNotification(order, "ORDER_CREATED",
                "New Order", "New order " + order.getOrderNumber() + " received");
        messagingTemplate.convertAndSend("/topic/kitchen", notification);
        messagingTemplate.convertAndSend("/topic/orders", notification);
    }

    public void notifyOrderStatusChanged(Order order) {
        NotificationDto notification = buildOrderNotification(order, "ORDER_STATUS_CHANGED",
                "Order Updated", "Order " + order.getOrderNumber() + " is now " + order.getStatus());
        messagingTemplate.convertAndSend("/topic/orders", notification);
        messagingTemplate.convertAndSend("/topic/kitchen", notification);

        if (order.getStatus() == Order.OrderStatus.READY) {
            NotificationDto waiterNotif = buildOrderNotification(order, "ORDER_READY",
                    "Order Ready", "Order " + order.getOrderNumber() + " is ready for pickup");
            messagingTemplate.convertAndSend("/topic/waiter", waiterNotif);
        }
    }

    public void notifyPaymentProcessed(Order order, Payment payment) {
        NotificationDto notification = NotificationDto.builder()
                .type("PAYMENT_PROCESSED")
                .title("Payment Received")
                .message("Payment for order " + order.getOrderNumber() + " - " + payment.getPaymentMethod())
                .referenceId(order.getId())
                .referenceType("ORDER")
                .timestamp(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend("/topic/orders", notification);
    }

    public void notifyTableStatusChanged(Long tableId, String status) {
        NotificationDto notification = NotificationDto.builder()
                .type("TABLE_STATUS_CHANGED")
                .title("Table Updated")
                .message("Table status changed to " + status)
                .referenceId(tableId)
                .referenceType("TABLE")
                .timestamp(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend("/topic/tables", notification);
    }

    private NotificationDto buildOrderNotification(Order order, String type, String title, String message) {
        return NotificationDto.builder()
                .type(type)
                .title(title)
                .message(message)
                .referenceId(order.getId())
                .referenceType("ORDER")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
