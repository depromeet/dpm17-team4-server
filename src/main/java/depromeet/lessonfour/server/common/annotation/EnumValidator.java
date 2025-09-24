package depromeet.lessonfour.server.common.annotation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, Enum<?>> {
  private Set<String> validValues;
  private Class<? extends Enum<?>> enumClass;

  @Override
  public void initialize(ValidEnum annotation) {
    this.enumClass = annotation.enumClass();
    validValues =
        Arrays.stream(this.enumClass.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
    if (value == null) return true;
    return value.getDeclaringClass().equals(enumClass) && validValues.contains(value.name());
  }
}
