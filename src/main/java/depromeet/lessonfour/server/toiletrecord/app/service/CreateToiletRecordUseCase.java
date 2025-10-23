package depromeet.lessonfour.server.toiletrecord.app.service;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordCreateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.response.ToiletRecordResponseDto;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEvent;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEventPublisher;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CreateToiletRecordUseCase {

  private final ToiletRecordService toiletRecordService;
  private final ToiletRecordEventPublisher toiletRecordEventPublisher;

  public ToiletRecordResponseDto create(Long userId, ToiletRecordCreateRequestDto dto) {
    ToiletRecordResponseDto response = toiletRecordService.create(userId, dto);
    toiletRecordEventPublisher.publishCreated(
        "CreateToiletRecordUseCase:create",
        new ToiletRecordEvent(userId, ActivityAt.from(response.occurredAt())));

    return response;
  }
}
