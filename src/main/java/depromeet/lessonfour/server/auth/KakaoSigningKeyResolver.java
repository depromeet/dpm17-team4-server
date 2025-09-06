package depromeet.lessonfour.server.auth;

import java.math.BigInteger;
import java.security.Key;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Locator;

public class KakaoSigningKeyResolver implements Locator<Key> {

  private final Map<String, Object> jwks;

  public KakaoSigningKeyResolver(Map<String, Object> jwks) {
    this.jwks = jwks;
  }

  @Override
  public Key locate(Header header) {
    if (header instanceof JwsHeader) {
      return resolveSigningKey((JwsHeader) header);
    }
    throw new IllegalArgumentException("Expected JwsHeader, got: " + header.getClass());
  }

  private Key resolveSigningKey(JwsHeader header) {
    String kid = header.getKeyId();
    System.out.println("Resolving signing key for kid: " + kid);

    // JWKS에서 해당 kid의 키 찾기
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
    System.out.println("Available keys in JWKS:");
    for (Map<String, Object> key : keys) {
      System.out.println(
          "  - kid: " + key.get("kid") + ", kty: " + key.get("kty") + ", use: " + key.get("use"));
    }

    for (Map<String, Object> key : keys) {
      if (kid.equals(key.get("kid"))) {
        System.out.println("Found matching key: " + key);

        // RSA 공개키 생성
        String n = (String) key.get("n");
        String e = (String) key.get("e");
        String kty = (String) key.get("kty");
        String use = (String) key.get("use");

        System.out.println(
            "Key details - kty: "
                + kty
                + ", use: "
                + use
                + ", n length: "
                + (n != null ? n.length() : "null")
                + ", e: "
                + e);

        if (n != null && e != null && "RSA".equals(kty)) {
          try {
            byte[] nBytes = Base64.getUrlDecoder().decode(n);
            byte[] eBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            System.out.println("Successfully created RSA public key for kid: " + kid);
            return publicKey;
          } catch (Exception ex) {
            System.out.println("Failed to create RSA key: " + ex.getMessage());
            throw new RuntimeException("Failed to create public key", ex);
          }
        } else {
          System.out.println(
              "Key validation failed - n: "
                  + (n != null)
                  + ", e: "
                  + (e != null)
                  + ", kty: "
                  + kty);
        }
      }
    }

    throw new RuntimeException("No matching key found for kid: " + kid);
  }
}
