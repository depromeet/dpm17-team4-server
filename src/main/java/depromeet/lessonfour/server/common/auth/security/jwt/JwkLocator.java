package depromeet.lessonfour.server.common.auth.security.jwt;

import java.math.BigInteger;
import java.security.Key;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Locator;

public class JwkLocator implements Locator<Key> {

  private final Map<String, Object> jwks;

  public JwkLocator(Map<String, Object> jwks) {
    this.jwks = jwks;
  }

  @Override
  public Key locate(Header header) {
    if (!(header instanceof JwsHeader)) {
      throw new IllegalArgumentException("Expected JwsHeader");
    }

    JwsHeader jwsHeader = (JwsHeader) header;
    String kid = jwsHeader.getKeyId();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

    for (Map<String, Object> key : keys) {
      if (kid.equals(key.get("kid"))) {
        return createPublicKey(key);
      }
    }

    throw new RuntimeException("No matching key found for kid: " + kid);
  }

  private Key createPublicKey(Map<String, Object> key) {
    String n = (String) key.get("n");
    String e = (String) key.get("e");
    String kty = (String) key.get("kty");

    if (n != null && e != null && "RSA".equals(kty)) {
      try {
        byte[] nBytes = Base64.getUrlDecoder().decode(n);
        byte[] eBytes = Base64.getUrlDecoder().decode(e);

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
      } catch (Exception ex) {
        throw new RuntimeException("Failed to create public key", ex);
      }
    }

    throw new RuntimeException("Unsupported key type: " + kty);
  }
}
