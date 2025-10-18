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

    if (userInfo.email == null) {
      throw new ServerException(AuthErrorCode.EMAIL_REQUIRED_FOR_OIDC);
    }

    if (userInfo.sub == null) {
      throw new ServerException(AuthErrorCode.SUB_REQUIRED_FOR_OIDC);
    }

    return userInfo;
  }
}
