package com.cedarco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        @NotBlank @Size(min = 6) private String password;
        private String phone;
    }

    @Data
    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data
    public static class TokenResponse {
        private Long id;
        private String accessToken;
        private String refreshToken;
        private String email;
        private String firstName;
        private String lastName;
        private String role;

        public TokenResponse(Long id, String accessToken, String refreshToken,
                             String email, String firstName, String lastName, String role) {
            this.id = id;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
        }
    }

    @Data
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }
}
