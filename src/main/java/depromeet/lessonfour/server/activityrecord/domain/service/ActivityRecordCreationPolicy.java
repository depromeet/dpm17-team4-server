package depromeet.lessonfour.server.activityrecord.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.app.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityRecordCreationPolicy {

  private final ActivityRecordRepository activityRecordRepository;

  public void validateNoDuplicateRecord(User user, LocalDateTime occurredAt) {
    LocalDate date = occurredAt.toLocalDate();
    LocalDateTime startOfDay = date.atStartOfDay();

    if (activityRecordRepository.existsByUserIdAndActivityAt(user.getId(), startOfDay)) {
      throw new ServerException(ErrorCode.CONFLICT);
    }
  }
}
