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

    @Value("${FIREBASE_CONFIG_JSON:}")
    private String firebaseConfigJson;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options;
                if (firebaseConfigJson != null && !firebaseConfigJson.isEmpty()) {
                    log.info("Initializing Firebase using FIREBASE_CONFIG_JSON environment variable");
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(new java.io.ByteArrayInputStream(firebaseConfigJson.getBytes())))
                            .build();
                } else {
                    log.info("Initializing Firebase using file: {}", firebaseConfigPath);
                    FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath);
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                }
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
            log.warn("CANNOT SEND NOTIFICATION: Firebase is NOT initialized. Check your FIREBASE_CONFIG_JSON or service account key.");
            return;
        }

        List<FcmToken> tokens = fcmTokenRepository.findByUser(user);
        log.info("Found {} tokens for user {}", tokens.size(), user.getEmail());
        
        for (FcmToken fcmToken : tokens) {
            try {
                log.info("Attempting to send FCM message to token: {}...", fcmToken.getToken().substring(0, Math.min(10, fcmToken.getToken().length())));
                
                Message message = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setWebpushConfig(createWebpushConfig(title, body, "https://cedar-co-app.vercel.app/account"))
                        .putData("orderId", String.valueOf(orderId))
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent message! Firebase response: {}", response);
            } catch (Exception e) {
                log.error("CRITICAL: Failed to send FCM message. Error: {}", e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("registration-token-not-registered")) {
                    log.info("Token is invalid, removing from database.");
                    fcmTokenRepository.delete(fcmToken);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public void broadcastNotification(String title, String body) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("CANNOT BROADCAST: Firebase not initialized.");
            return;
        }

        List<FcmToken> allTokens = fcmTokenRepository.findAll();
        log.info("Broadcasting message to {} device(s)", allTokens.size());

        for (FcmToken fcmToken : allTokens) {
            try {
                Message message = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setWebpushConfig(createWebpushConfig(title, body, "https://cedar-co-app.vercel.app/shop"))
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Broadcast sent to token {}. Response: {}", fcmToken.getToken().substring(0, 10), response);
            } catch (Exception e) {
                log.error("Failed to send broadcast to token: {}, Error: {}", fcmToken.getToken(), e.getMessage());
                if (e.getMessage() != null && (e.getMessage().contains("registration-token-not-registered") || e.getMessage().contains("invalid-registration-token"))) {
                    log.info("Cleaning up invalid token...");
                    fcmTokenRepository.delete(fcmToken);
                }
            }
        }
    }

    private com.google.firebase.messaging.WebpushConfig createWebpushConfig(String title, String body, String link) {
        return com.google.firebase.messaging.WebpushConfig.builder()
                .setNotification(com.google.firebase.messaging.WebpushNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setIcon("/assets/icons/icon-72x72.png")
                        .setVibrate(new int[]{200, 100, 200})
                        .build())
                .setFcmOptions(com.google.firebase.messaging.WebpushFcmOptions.builder()
                        .setLink(link)
                        .build())
                .build();
    }
}
