package com.hanainplan.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase 설정 클래스
 * - Firebase Admin SDK 초기화
 * - FCM (Firebase Cloud Messaging) 설정
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.file:firebase-service-account.json}")
    private String firebaseConfigFile;

    @PostConstruct
    public void initialize() {
        try {
            // Firebase가 이미 초기화되어 있는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;
                
                try {
                    // 클래스패스에서 파일 찾기
                    ClassPathResource resource = new ClassPathResource(firebaseConfigFile);
                    serviceAccount = resource.getInputStream();
                    log.info("Firebase config file found in classpath: {}", firebaseConfigFile);
                } catch (IOException e) {
                    // 클래스패스에 없으면 파일 시스템에서 찾기
                    log.warn("Firebase config file not found in classpath, trying file system: {}", firebaseConfigFile);
                    serviceAccount = new FileInputStream(firebaseConfigFile);
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            } else {
                log.info("Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            log.error("Firebase initialization failed. FCM notifications will not work.", e);
            log.error("Please ensure {} is available in classpath or file system", firebaseConfigFile);
            // Firebase 없이도 애플리케이션이 실행되도록 예외를 던지지 않음
        }
    }
}





