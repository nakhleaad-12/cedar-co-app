package com.cedarco.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role;
        private boolean active;
        private LocalDateTime createdAt;
    }
    @Data
    public static class ProfileUpdateRequest {
        private String firstName;
        private String lastName;
        private String phone;
    }

    @Data
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Data
    public static class AddressRequest {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private boolean isDefault;
    }

    @Data
    public static class AddressResponse {
        private Long id;
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private boolean isDefault;
    }

    @Data
    public static class FullResponse {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role;
        private boolean active;
        private java.util.List<AddressResponse> addresses;
        private LocalDateTime createdAt;
    }
}
