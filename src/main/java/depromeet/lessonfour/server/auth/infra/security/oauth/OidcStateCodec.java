package depromeet.lessonfour.server.auth.infra.security.oauth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import depromeet.lessonfour.server.auth.domain.vo.StateData;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OidcStateCodec {

  @Value("${frontend.url}")
  private String defaultRedirectUri;

  private final ObjectMapper objectMapper;

  public String encode(String redirectUri, String responseType) {
    try {
      Map<String, String> stateMap =
          Map.of(
              "redirectUri", redirectUri,
              "responseType", responseType);
      String json = objectMapper.writeValueAsString(stateMap);
      return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encode state", e);
    }
  }

  public StateData decode(String encodedState) {
    try {
      String json = new String(Base64.getUrlDecoder().decode(encodedState), StandardCharsets.UTF_8);
      Map<String, String> stateMap = objectMapper.readValue(json, new TypeReference<>() {});
      return new StateData(
          stateMap.getOrDefault("redirectUri", defaultRedirectUri),
          stateMap.getOrDefault("responseType", "token"));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to decode state", e);
    }
  }
}
