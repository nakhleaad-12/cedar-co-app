package com.cedarco.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class CreateRequest {
        private Long addressId;
        // Snapshot shipping address (alternative)
        private String shippingStreet;
        private String shippingCity;
        private String shippingRegion;
        private String shippingCountry;
        private String paymentMethod;
        private String couponCode;
    }

    @Data
    public static class Response {
        private Long id;
        private String status;
        private String paymentStatus;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal total;
        private String couponCode;
        private String paymentMethod;
        private String shippingCity;
        private String shippingCountry;
        private List<ItemResponse> items;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ItemResponse {
        private Long productId;
        private String productName;
        private String size;
        private String color;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    @Data
    public static class UpdateStatusRequest {
        private String status;
    }

    @Data
    public static class UpdatePaymentStatusRequest {
        private String paymentStatus;
    }
}
