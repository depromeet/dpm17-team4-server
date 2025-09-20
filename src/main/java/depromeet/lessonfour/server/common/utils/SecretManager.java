package depromeet.lessonfour.server.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import depromeet.lessonfour.server.common.annotation.Value;

@Component
public class SecretManager {
  private static final String HMAC_SHA256 = "HmacSHA256";

  @Value("${ncp.secret-manager-url}")
  private String secretManagerUrl;

  @Value("${ncp.access-key}")
  private String accessKey;

  @Value("${ncp.secret-key}")
  private String secretKey;

  @Value("${ncp.secret-id}")
  private String secretId;

  private final RestTemplate restTemplate;

  public SecretManager(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private String makeSignature(String uri, String method)
      throws NoSuchAlgorithmException, InvalidKeyException {
    long timestamp = System.currentTimeMillis();
    String message = method + " " + uri + "\n" + timestamp + "\n" + accessKey;

    Mac mac = Mac.getInstance(HMAC_SHA256);
    SecretKeySpec secretKeySpec =
        new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
    mac.init(secretKeySpec);

    byte[] signature = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(signature);
  }

  private HttpHeaders getHeaders(String uri, String method)
      throws NoSuchAlgorithmException, InvalidKeyException {
    long timestamp = System.currentTimeMillis();
    String signature = makeSignature(uri, method);

    HttpHeaders headers = new HttpHeaders();
    headers.set("x-ncp-apigw-timestamp", String.valueOf(timestamp));
    headers.set("x-ncp-iam-access-key", accessKey);
    headers.set("x-ncp-apigw-signature-v2", signature);
    headers.set("Content-Type", "application/json");
    return headers;
  }

  public ResponseEntity<String> getSecrets() throws NoSuchAlgorithmException, InvalidKeyException {
    String uri = "/api/v1/secrets";
    HttpHeaders headers = getHeaders(uri, "GET");
    HttpEntity<String> entity = new HttpEntity<>(headers);
    return restTemplate.exchange(secretManagerUrl + uri, HttpMethod.GET, entity, String.class);
  }

  public Map<String, Object> getSecretValues() throws NoSuchAlgorithmException, InvalidKeyException {
    return getSecretValues(secretId);
  }

  public Map<String, Object> getSecretValues(String secretId) throws NoSuchAlgorithmException, InvalidKeyException {
    String uri = "/api/v1/secrets/" + secretId + "/values";
    HttpHeaders headers = getHeaders(uri, "GET");
    HttpEntity<String> entity = new HttpEntity<>(headers);
    
    ResponseEntity<String> response = restTemplate.exchange(secretManagerUrl + uri, HttpMethod.GET, entity, String.class);
    
    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      try {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(response.getBody());
        if (jsonNode.has("data") && jsonNode.get("data").has("decryptedSecretChain")) {
          JsonNode secretChain = jsonNode.get("data").get("decryptedSecretChain");
          if (secretChain.has("active")) {
            String activeSecretJson = secretChain.get("active").asText();
            JsonNode activeSecrets = objectMapper.readTree(activeSecretJson);
            Map<String, Object> secrets = new HashMap<>();
            activeSecrets.properties().forEach(entry -> {
              secrets.put(entry.getKey(), entry.getValue().asText());
            });
            return secrets;
          }
        }
        return new HashMap<>();
      } catch (Exception e) {
        throw new RuntimeException("시크릿 응답 파싱 실패", e);
      }
    }
    return new HashMap<>();
  }
}
