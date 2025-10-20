package depromeet.lessonfour.server.auth.app.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AppleUserCache {
  private final Map<String, String> cache = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public AppleUserCache(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void save(String code, String userJson) {
    cache.put(code, userJson);
  }

  public Map<String, Object> getUser(String code) {
    String userJson = cache.remove(code);
    if (userJson == null) return null;
    try {
      return objectMapper.readValue(userJson, new TypeReference<>() {});
    } catch (Exception e) {
      return null;
    }
  }

  public void remove(String code) {
    cache.remove(code);
  }
}
