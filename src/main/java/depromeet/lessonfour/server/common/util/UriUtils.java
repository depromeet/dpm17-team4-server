package depromeet.lessonfour.server.common.util;

import java.net.URI;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UriUtils {

  public String extractDomain(String uriString) {
    try {
      URI uri = URI.create(uriString);
      return uri.getHost();
    } catch (Exception e) {
      return null;
    }
  }
}
