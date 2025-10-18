package depromeet.lessonfour.server.toiletrecord.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class DeleteToiletRecordUseCase {

  private final ToiletRecordRepository toiletRecordRepository;

  public void delete(Long userId, Long recordId) {
    ToiletRecord record =
        toiletRecordRepository
            .findById(userId, recordId)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));
    record.delete();
  }
}
