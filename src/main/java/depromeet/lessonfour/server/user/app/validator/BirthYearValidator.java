package depromeet.lessonfour.server.user.app.validator;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BirthYearValidator implements ConstraintValidator<ValidBirthYear, Integer> {

  private static final int MIN_BIRTH_YEAR = 1900;

  @Override
  public boolean isValid(Integer birthYear, ConstraintValidatorContext context) {
    if (birthYear == null) {
      return true; // null 값은 허용 (선택적 필드)
    }

    int currentYear = LocalDate.now().getYear();
    return birthYear >= MIN_BIRTH_YEAR && birthYear <= currentYear;
  }
}
