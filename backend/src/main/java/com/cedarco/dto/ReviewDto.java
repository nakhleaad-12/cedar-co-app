package com.cedarco.dto;

import lombok.Data;

public class ReviewDto {

    @Data
    public static class CreateRequest {
        private Integer rating;
        private String title;
        private String body;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private String userName;
        private Integer rating;
        private String title;
        private String body;
        private boolean verified;
        private String createdAt;
    }
}
