package depromeet.lessonfour.server.auth.infra.security.oauth.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAdminClient {

  private RestTemplate restTemplate;

  @Value("${spring.security.oauth2.client.registration.kakao.admin-key}")
  private String adminKey;

  @Value("${spring.security.oauth2.client.registration.kakao.unlink-uri}")
  private String unlinkUri;

  @PostConstruct
  public void initRestTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(15000); // 15초 연결 타임아웃
    factory.setReadTimeout(15000); // 15초 읽기 타임아웃

    this.restTemplate = new RestTemplate(factory);
    log.debug("RestTemplate initialized with 30 second timeout");
  }

  public void unlinkUser(String userId) {
    log.debug("Unlinking Kakao user: {}", userId);

    try {
      HttpHeaders headers = createHeaders();
      MultiValueMap<String, String> requestBody = createRequestBody(userId);
      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<String> response =
          restTemplate.exchange(unlinkUri, HttpMethod.POST, entity, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Kakao user unlink successful for user {}: {}", userId, response.getBody());
      } else {
        log.warn(
            "Kakao user unlink failed for user {} with status: {}",
            userId,
            response.getStatusCode());
      }
    } catch (Exception e) {
      log.error("Failed to unlink Kakao user {}", userId, e);
      throw new RuntimeException("Kakao user unlink failed", e);
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "KakaoAK " + adminKey);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return headers;
  }

  private MultiValueMap<String, String> createRequestBody(String userId) {
    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("target_id_type", "user_id");
    requestBody.add("target_id", userId);
    return requestBody;
  }
}
