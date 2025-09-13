package depromeet.lessonfour.server.auth.security.jwt;

import java.math.BigInteger;
import java.security.Key;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import depromeet.lessonfour.server.auth.service.code.AuthErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
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
      throw new ServerException(AuthErrorCode.INVALID_OIDC_TOKEN);
    }

    JwsHeader jwsHeader = (JwsHeader) header;
    String kid = jwsHeader.getKeyId();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
    if (keys == null) {
      throw new ServerException(AuthErrorCode.JWKS_RESPONSE_INVALID);
    }

    for (Map<String, Object> key : keys) {
      if (kid.equals(key.get("kid"))) {
        return createPublicKey(key);
      }
    }

    throw new ServerException(AuthErrorCode.JWKS_KEY_NOT_FOUND);
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
        // 로컬에서 공개키 구성 실패 → 내부 서버 오류(500)
        throw new ServerException(AuthErrorCode.JWKS_PUBLIC_KEY_BUILD_FAILED);
      }
    }

    // RSA 외 키 타입 → 우리 서버가 검증 불가 → 인증 실패(401)
    throw new ServerException(AuthErrorCode.JWKS_UNSUPPORTED_KEY_TYPE);
  }
}
