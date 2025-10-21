package depromeet.lessonfour.server.toiletrecord.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordCreateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordUpdateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.response.ToiletRecordResponseDto;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.user.app.service.UserQueryService;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ToiletRecordService {

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
            ActivityAt.from(dto.occurredAt()));

    toiletRecordRepository.save(record);
    return ToiletRecordResponseDto.of(record);
  }

  public ToiletRecordResponseDto update(
      Long userId, Long recordId, ToiletRecordUpdateRequestDto dto) {
    ToiletRecord record =
        toiletRecordRepository
            .findById(userId, recordId)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));

    record.applyPatch(
        dto.isSuccessful(),
        dto.color(),
        dto.shape(),
        dto.pain(),
        dto.duration(),
        dto.note(),
        ActivityAt.from(dto.occurredAt()));

    return ToiletRecordResponseDto.of(record);
  }
}
