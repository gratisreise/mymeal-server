package com.mymealserver.external.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

  private final ResourceLoader resourceLoader;

  @Value("${fcm.key-path}")
  private String keyPath;

  @Bean
  public FirebaseMessaging firebaseMessaging() throws IOException {

    Resource resource = resourceLoader.getResource(keyPath);

    // 리소스 존재 여부 확인 (방어 코드)
    if (!resource.exists()) {
      throw new IOException("FCM 키 파일을 찾을 수 없습니다: " + keyPath);
    }

    try (InputStream serviceAccount = resource.getInputStream()) {
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      FirebaseApp firebaseApp =
          FirebaseApp.getApps().isEmpty()
              ? FirebaseApp.initializeApp(options)
              : FirebaseApp.getInstance();

      return FirebaseMessaging.getInstance(firebaseApp);
    }
  }
}
