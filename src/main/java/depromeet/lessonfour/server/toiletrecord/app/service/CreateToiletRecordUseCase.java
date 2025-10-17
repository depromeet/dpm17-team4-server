package depromeet.lessonfour.server.toiletrecord.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordCreateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.response.ToiletRecordResponseDto;
import depromeet.lessonfour.server.toiletrecord.app.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.user.app.service.UserQueryService;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class CreateToiletRecordUseCase {

  private final UserQueryService userQueryService;
  private final ToiletRecordRepository toiletRecordRepository;

  public ToiletRecordResponseDto create(Long userId, ToiletRecordCreateRequestDto dto) {
    User user = userQueryService.getActivatedUserById(userId);

    ToiletRecord record =
        ToiletRecord.register(
            user,
            dto.isSuccessful(),
            dto.color(),
            dto.shape(),
            dto.pain(),
            dto.duration(),
            dto.note(),
            dto.occurredAt());

    toiletRecordRepository.save(record);
    return ToiletRecordResponseDto.of(record);
  }
}
