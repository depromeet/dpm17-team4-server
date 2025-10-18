package depromeet.lessonfour.server.toiletrecord.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordUpdateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.response.ToiletRecordResponseDto;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class UpdateToiletRecordUseCase {

  private final ToiletRecordRepository toiletRecordRepository;

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

    // JPA dirty checking
    return ToiletRecordResponseDto.of(record);
  }
}
