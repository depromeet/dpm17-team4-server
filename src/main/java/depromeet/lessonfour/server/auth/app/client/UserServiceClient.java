package depromeet.lessonfour.server.auth.app.client;

import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;
import depromeet.lessonfour.server.user.domain.entity.User;

public interface UserServiceClient {

  User findOrCreate(
      String email,
      String nickname,
      String profileImage,
      SocialProvider socialProvider,
      String providerId);
}
