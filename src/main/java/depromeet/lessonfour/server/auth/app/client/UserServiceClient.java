package depromeet.lessonfour.server.auth.app.client;

import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;

public interface UserServiceClient {

  User findOrCreate(String email, String nickname, String profileImage, Provider provider);
}
