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
}
