package depromeet.lessonfour.server.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoAuthService {

  private final RestTemplate restTemplate;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;

  public KakaoAuthService(
      RestTemplate restTemplate,
      @Value("${kakao.client-id}") String clientId,
      @Value("${kakao.client-secret}") String clientSecret,
      @Value("${kakao.redirect-uri}") String redirectUri) {
    this.restTemplate = restTemplate;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
  }

  public Map<String, Object> getToken(String code) {
    String tokenUrl = "https://kauth.kakao.com/oauth/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("code", code);
    params.add("redirect_uri", redirectUri);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        tokenUrl, 
        HttpMethod.POST, 
        request, 
        new ParameterizedTypeReference<Map<String, Object>>() {}
    );

    if (response.getStatusCode().value() != 200) {
      throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
    }

    Map<String, Object> tokenResponse = response.getBody();
    System.out.println("TOKEN: " + tokenResponse);
    return tokenResponse;
  }
}
