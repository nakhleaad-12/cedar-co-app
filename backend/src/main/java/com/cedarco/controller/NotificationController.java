package com.cedarco.controller;

import com.cedarco.entity.User;
import com.cedarco.repository.UserRepository;
import com.cedarco.service.FcmService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    @PostMapping("/tokens")
    public ResponseEntity<?> saveToken(@AuthenticationPrincipal UserDetails userDetails, @RequestBody TokenRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
            fcmService.saveToken(user, request.getToken());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error saving FCM token: ", e);
            return ResponseEntity.status(500).body("Error saving token: " + e.getMessage());
        }
    }

    @GetMapping("/test-push")
    public ResponseEntity<?> testPush(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("Unauthenticated");
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        log.info("Triggering test push notification for user: {}", user.getEmail());
        fcmService.sendOrderNotification(user, "Test Notification", "This is a test notification from Cedar & Co.", 0L);
        
        return ResponseEntity.ok("Test push triggered. Check server logs for delivery status.");
    }

    @DeleteMapping("/tokens/{token}")
    public ResponseEntity<?> deleteToken(@PathVariable String token) {
        fcmService.deleteToken(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@AuthenticationPrincipal UserDetails userDetails, @RequestBody BroadcastRequest request) {
        if (userDetails == null) return ResponseEntity.status(401).body("Unauthenticated");
        
        // Security check is also handled by SecurityConfig, but extra check here
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body("Only Admins can broadcast notifications");
        }

        log.info("Admin {} is triggering a global broadcast: {}", user.getEmail(), request.getTitle());
        fcmService.broadcastNotification(request.getTitle(), request.getBody());
        
        return ResponseEntity.ok("Broadcast triggered successfully.");
    }

    @Data
    public static class TokenRequest {
        private String token;
    }

    @Data
    public static class BroadcastRequest {
        private String title;
        private String body;
    }
}
