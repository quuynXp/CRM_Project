package com.crm.notification.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config.path:/app/config/service-account-key.json}")
    private String configPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        FirebaseApp firebaseApp = null;
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();

        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    firebaseApp = app;
                    break;
                }
            }
        }

        if (firebaseApp == null) {
            GoogleCredentials credentials = loadCredentials();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            firebaseApp = FirebaseApp.initializeApp(options);
        }

        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private GoogleCredentials loadCredentials() throws IOException {
        InputStream serviceAccount = null;

        try {
            serviceAccount = new FileInputStream(configPath);
            logger.info("Loaded Firebase credentials from File System: {}", configPath);
        } catch (FileNotFoundException e1) {
            logger.warn("Config file not found at {}. Trying Classpath...", configPath);
            
            try {
                ClassPathResource resource = new ClassPathResource("service-account-key.json");
                if (resource.exists()) {
                    serviceAccount = resource.getInputStream();
                    logger.info("Loaded Firebase credentials from Classpath.");
                }
            } catch (Exception ex) {
                logger.error("Error loading from classpath", ex);
            }
        }

        if (serviceAccount == null) {
            throw new IOException("CRITICAL: Could not find Firebase credentials. Checked: " + configPath + " and classpath.");
        }

        return GoogleCredentials.fromStream(serviceAccount);
    }
}