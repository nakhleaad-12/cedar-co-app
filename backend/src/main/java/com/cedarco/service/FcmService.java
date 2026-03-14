package com.cedarco.service;

import com.cedarco.entity.FcmToken;
import com.cedarco.entity.User;
import com.cedarco.repository.FcmTokenRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;

    @Value("${app.firebase-config-path:firebase-service-account.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (Exception e) {
            log.warn("Firebase initialization failed. Push notifications will not be sent. Error: {}", e.getMessage());
        }
    }

    @Transactional
    public void saveToken(User user, String token) {
        Optional<FcmToken> existing = fcmTokenRepository.findByToken(token);
        if (existing.isPresent()) {
            FcmToken fToken = existing.get();
            fToken.setUser(user);
            fcmTokenRepository.save(fToken);
        } else {
            FcmToken fToken = new FcmToken();
            fToken.setUser(user);
            fToken.setToken(token);
            fcmTokenRepository.save(fToken);
        }
    }

    @Transactional
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }

    public void sendOrderNotification(User user, String title, String body, Long orderId) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("Firebase not initialized, skipping notification to user {}", user.getEmail());
            return;
        }

        List<FcmToken> tokens = fcmTokenRepository.findByUser(user);
        for (FcmToken fcmToken : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("orderId", String.valueOf(orderId))
                        .putData("click_action", "FLUTTER_NOTIFICATION_CLICK") // For mobile, but good to have
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent message: " + response);
            } catch (Exception e) {
                log.error("Failed to send FCM message to token: {}, Error: {}", fcmToken.getToken(), e.getMessage());
                if (e.getMessage().contains("registration-token-not-registered")) {
                    fcmTokenRepository.delete(fcmToken);
                }
            }
        }
    }
}
