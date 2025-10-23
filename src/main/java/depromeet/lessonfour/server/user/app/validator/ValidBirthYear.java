package depromeet.lessonfour.server.user.app.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = BirthYearValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthYear {
  String message() default "출생연도는 1900년보다 크고 현재 연도보다 작거나 같아야 합니다";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
