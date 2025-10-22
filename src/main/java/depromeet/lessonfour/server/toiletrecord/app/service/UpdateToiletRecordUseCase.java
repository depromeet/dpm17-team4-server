package depromeet.lessonfour.server.toiletrecord.app.service;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordUpdateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.response.ToiletRecordResponseDto;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEvent;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEventPublisher;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class UpdateToiletRecordUseCase {

  private final ToiletRecordService toiletRecordService;
  private final ToiletRecordEventPublisher toiletRecordEventPublisher;

  public ToiletRecordResponseDto update(
      Long userId, Long recordId, ToiletRecordUpdateRequestDto dto) {
    ToiletRecordResponseDto response = toiletRecordService.update(userId, recordId, dto);
    toiletRecordEventPublisher.publishUpdated(
        "UpdateToiletRecordUseCase:update",
        new ToiletRecordEvent(userId, ActivityAt.from(response.occurredAt())));

    return response;
  }
}
