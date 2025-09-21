package depromeet.lessonfour.server.activityrecord.app.service;

import java.time.LocalDate;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.app.dto.response.GetActivityRecordsResponse;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.infra.repository.ActivityRecordQueryRepository;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QueryActivityRecordUseCase {

  private final ActivityRecordQueryRepository queryRepository;

  public GetActivityRecordsResponse getActivityRecord(Long userId, LocalDate date) {
    ActivityRecord record =
        queryRepository
            .findByUserIdAndOccurredAt(userId, date)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));
    return GetActivityRecordsResponse.from(
        record.getId(),
        record.getWaterIntakeCups(),
        record.getStressLevel(),
        record.getFoodRecords(),
        record.getActivityAt());
  }
}
