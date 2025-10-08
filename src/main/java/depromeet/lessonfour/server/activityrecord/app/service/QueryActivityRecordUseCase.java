package depromeet.lessonfour.server.activityrecord.app.service;

import java.time.LocalDate;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.app.dto.response.GetActivityRecordsResponse;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QueryActivityRecordUseCase {

  private final ActivityRecordRepository activityRecordRepository;

  public GetActivityRecordsResponse getActivityRecord(Long userId, LocalDate date) {
    ActivityRecord record =
        activityRecordRepository
            .findByActivityAt(userId, ActivityAt.of(date))
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));

    return GetActivityRecordsResponse.from(
        record.getId(),
        record.getWaterIntakeCups(),
        record.getStressLevel(),
        record.getFoodRecords(),
        record.getActivityAt());
  }
}
