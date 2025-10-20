package depromeet.lessonfour.server.notification.infra.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

  @PostConstruct
  public void initialize() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      try (InputStream serviceAccount =
          new ClassPathResource("firebase-adminsdk.json").getInputStream()) {

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase Admin SDK initialized successfully");
      } catch (IOException e) {
        log.error("Failed to initialize Firebase Admin SDK", e);
        throw e;
      }
    }
  }
}
