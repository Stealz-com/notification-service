package com.ecommerce.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {
    private String orderNumber;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal totalAmount;
    private OrderAddress shippingAddress;
    private java.util.List<LineItem> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LineItem {
        private String skuCode;
        private BigDecimal price;
        private Integer quantity;
    }
}
