package depromeet.lessonfour.server.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderTest {

  @Autowired PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("PasswordEncoder의 prefix는 {bcrypt}로 시작해야 한다.")
  void givenPasswordEncoder_whenEncode_thenPrefixShouldBeBcrypt() {
    String rawPassword = "password";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    // Check if the encoded password starts with {bcrypt}
    assertThat(encodedPassword).startsWith("{bcrypt}");
  }
}
