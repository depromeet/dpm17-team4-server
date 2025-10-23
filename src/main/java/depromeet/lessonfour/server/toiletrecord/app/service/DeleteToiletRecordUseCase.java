package depromeet.lessonfour.server.toiletrecord.app.service;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEvent;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEventPublisher;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class DeleteToiletRecordUseCase {

  private final ToiletRecordService toiletRecordService;
  private final ToiletRecordEventPublisher toiletRecordEventPublisher;

  public void delete(Long userId, Long recordId) {
    ToiletRecord record = toiletRecordService.delete(userId, recordId);
    toiletRecordEventPublisher.publishDeleted(
        "DeleteToiletRecordUseCase:delete", new ToiletRecordEvent(userId, record.getActivityAt()));
  }
}
