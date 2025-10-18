package depromeet.lessonfour.server.activityrecord.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class DeleteActivityRecordUseCase {

  private final ActivityRecordRepository activityRecordRepository;

  public void deleteActivityRecord(Long userId, Long activityRecordId) {
    ActivityRecord activityRecord =
        activityRecordRepository
            .findById(activityRecordId)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));

    if (!activityRecord.isOwnedBy(userId)) {
      throw new ServerException(ErrorCode.DATA_NOT_FOUND);
    }

    activityRecord.delete();
  }
}
