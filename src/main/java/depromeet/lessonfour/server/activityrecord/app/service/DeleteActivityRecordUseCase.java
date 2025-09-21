package depromeet.lessonfour.server.activityrecord.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.infra.repository.JpaActivityRecordRepository;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class DeleteActivityRecordUseCase {

  private final JpaActivityRecordRepository activityRecordRepository;

  public void deleteActivityRecord(Long userId, Long activityRecordId) {
    ActivityRecord activityRecord =
        activityRecordRepository
            .findByUser_IdAndIdAndIsDeletedFalse(userId, activityRecordId)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));
    activityRecord.delete();
  }
}
