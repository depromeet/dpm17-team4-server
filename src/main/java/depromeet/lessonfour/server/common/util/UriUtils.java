package depromeet.lessonfour.server.common.util;

import java.net.URI;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class UriUtils {

  public String extractDomain(String uriString) {
    try {
      URI uri = URI.create(uriString);
      return uri.getHost();
    } catch (IllegalArgumentException e) {
      log.warn("Invalid URI: {}", uriString, e);
      return null;
    }
  }
}
