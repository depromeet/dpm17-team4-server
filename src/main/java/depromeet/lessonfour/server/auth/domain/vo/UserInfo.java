package depromeet.lessonfour.server.auth.domain.vo;

import java.util.Map;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.Getter;

@Getter
public class UserInfo {

  private String email;
  private String nickname;
  private String picture;
  private String sub;

  public static UserInfo from(Map<String, Object> claims) {
    UserInfo userInfo = new UserInfo();
    userInfo.email = (String) claims.get("email");
    userInfo.nickname = (String) claims.get("nickname");
    userInfo.picture = (String) claims.get("picture");
    userInfo.sub = (String) claims.get("sub");

    if (userInfo.sub == null) {
      throw new ServerException(AuthErrorCode.SUB_REQUIRED_FOR_OIDC);
    }

    if (userInfo.nickname == null) {
      userInfo.nickname = generateDefaultNickname(userInfo.email);
    }

    return userInfo;
  }

  private static String generateDefaultNickname(String email) {
    if (email == null) {
      return "user";
    }
    // 이메일 @ 앞부분 사용 (최대 32자)
    String emailPrefix = email.split("@")[0];
    return emailPrefix.length() > 32 ? emailPrefix.substring(0, 32) : emailPrefix;
  }
}
