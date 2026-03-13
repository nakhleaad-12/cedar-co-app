package com.cedarco.dto;

import lombok.Data;

import java.math.BigDecimal;

public class AdminDto {

    @Data
    public static class DashboardStats {
        private long totalProducts;
        private long totalOrders;
        private long totalCustomers;
        private long pendingOrders;
        private BigDecimal totalRevenue;
    }
}
