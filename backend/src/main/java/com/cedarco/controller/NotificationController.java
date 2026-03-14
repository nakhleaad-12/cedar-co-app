package com.cedarco.controller;

import com.cedarco.entity.User;
import com.cedarco.repository.UserRepository;
import com.cedarco.service.FcmService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    @PostMapping("/tokens")
    public ResponseEntity<?> saveToken(@AuthenticationPrincipal UserDetails userDetails, @RequestBody TokenRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        fcmService.saveToken(user, request.getToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tokens/{token}")
    public ResponseEntity<?> deleteToken(@PathVariable String token) {
        fcmService.deleteToken(token);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class TokenRequest {
        private String token;
    }
}
