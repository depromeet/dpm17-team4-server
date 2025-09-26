package depromeet.lessonfour.server.activityrecord.infra.support;

import java.time.LocalDateTime;

import depromeet.lessonfour.server.activityrecord.domain.vo.ActivityAt;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActivityAtConverter implements AttributeConverter<ActivityAt, LocalDateTime> {

  @Override
  public LocalDateTime convertToDatabaseColumn(ActivityAt attribute) {
    if (attribute == null) return null;
    return attribute.toDateTime();
  }

  @Override
  public ActivityAt convertToEntityAttribute(LocalDateTime dbData) {
    if (dbData == null) return null;
    return ActivityAt.from(dbData);
  }
}
