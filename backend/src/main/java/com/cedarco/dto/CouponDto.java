package com.cedarco.dto;

import lombok.Data;

import java.math.BigDecimal;

public class CouponDto {

    @Data
    public static class ValidateRequest {
        private String code;
        private BigDecimal orderAmount;
    }

    @Data
    public static class ValidateResponse {
        private boolean valid;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal discountAmount;
        private String message;
    }

    @Data
    public static class CreateRequest {
        private String code;
        private String discountType; // PERCENT or FIXED
        private BigDecimal discountValue;
        private BigDecimal minOrderAmount;
        private Integer usageLimit;
        private String expiryDate; // ISO date string
    }
}
