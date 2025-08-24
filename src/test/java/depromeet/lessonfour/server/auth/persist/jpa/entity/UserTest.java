package depromeet.lessonfour.server.auth.persist.jpa.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class UserTest {

  @Autowired private UserRepository userRepository;

  @Autowired private Validator validator;

  @BeforeEach
  void setup() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("팩토리 메서드로 사용자를 생성할 수 있다")
  void givenUserInfo_whenRegister_thenUserCreated() {
    // given
    String email = "test@example.com";
    String nickname = "testuser";
    String password = "password123";

    // when
    User user = User.register(email, nickname, password);

    // then
    assertThat(user)
        .extracting("email", "nickname", "password")
        .containsExactly(email, nickname, password);
  }

  @Test
  @DisplayName("사용자를 데이터베이스에 저장할 수 있다")
  void givenUser_whenSaved_thenUserPersisted() {
    // given
    String email = "test@example.com";
    String nickname = "testuser";
    String password = "password123";
    User user = User.register(email, nickname, password);

    // when
    User savedUser = userRepository.save(user);

    // then
    assertThat(savedUser.getId()).isNotNull().isInstanceOf(UUID.class);
    assertThat(savedUser)
        .extracting("email", "nickname", "password")
        .containsExactly(email, nickname, password);
  }

  @Test
  @DisplayName("이메일이 null이면 validation 에러가 발생한다")
  void givenUserWithNullEmail_whenValidate_thenValidationFails() {
    // given
    String nickname = "testuser";
    String password = "password123";

    // when
    User user = User.register(null, nickname, password);

    // then
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    assertThat(violations).hasSize(1);
    assertThat(violations)
        .extracting(ConstraintViolation::getPropertyPath)
        .extracting(Object::toString)
        .contains("email");
  }

  @Test
  @DisplayName("닉네임이 null이면 validation 에러가 발생한다")
  void givenUserWithNullNickname_whenValidate_thenValidationFails() {
    // given
    String email = "test@example.com";
    String password = "password123";

    // when
    User user = User.register(email, null, password);

    // then
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    assertThat(violations).hasSize(1);
    assertThat(violations)
        .extracting(ConstraintViolation::getPropertyPath)
        .extracting(Object::toString)
        .contains("nickname");
  }

  @Test
  @DisplayName("사용자는 기본적으로 USER 역할을 가진다")
  void givenUserWithRole_whenSaved_thenRolePersisted() {
    // given
    String email = "test@example.com";
    String nickname = "testuser";
    String password = "password123";

    // when
    User user = User.register(email, nickname, password);

    // then
    assertThat(user.getAuthority()).isEqualTo("ROLE_USER");
  }

  @Test
  @DisplayName("리프레시 토큰을 설정할 수 있다")
  void givenUserWithRefreshToken_whenSaved_thenRefreshTokenPersisted() {
    // given
    String email = "test@example.com";
    String nickname = "testuser";
    String password = "password123";
    User user = User.register(email, nickname, password);

    // when
    user.storeRefreshToken("sample-refresh-token");

    // then
    assertThat(user.getRefreshToken()).isEqualTo("sample-refresh-token");
  }

  @Test
  @DisplayName("동일한 이메일을 가진 사용자를 저장하면 DataIntegrityViolationException이 발생한다")
  void givenDuplicateEmail_whenSave_thenThrowsDataIntegrityViolationException() {
    // given
    String duplicateEmail = "duplicate@example.com";
    User firstUser = User.register(duplicateEmail, "firstuser", "password123");
    User secondUser = User.register(duplicateEmail, "seconduser", "password456");

    // when
    userRepository.save(firstUser);

    // then
    assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("동일한 닉네임을 가진 사용자를 저장하면 DataIntegrityViolationException이 발생한다")
  void givenDuplicateNickname_whenSave_thenThrowsDataIntegrityViolationException() {
    // given
    String duplicateNickname = "duplicatenick";
    User firstUser = User.register("first@example.com", duplicateNickname, "password123");
    User secondUser = User.register("second@example.com", duplicateNickname, "password456");

    // when
    userRepository.save(firstUser);

    // then
    assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("이메일과 닉네임이 모두 다르면 사용자를 정상적으로 저장할 수 있다")
  void givenDifferentEmailAndNickname_whenSave_thenBothUsersSaved() {
    // given
    User firstUser = User.register("first@example.com", "firstuser", "password123");
    User secondUser = User.register("second@example.com", "seconduser", "password456");

    // when
    User savedFirstUser = userRepository.save(firstUser);
    User savedSecondUser = userRepository.save(secondUser);

    // then
    assertThat(savedFirstUser.getId()).isNotNull();
    assertThat(savedSecondUser.getId()).isNotNull();
    assertThat(savedFirstUser.getId()).isNotEqualTo(savedSecondUser.getId());
  }
}
