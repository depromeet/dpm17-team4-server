package depromeet.lessonfour.server.toiletrecord.infra.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.toiletrecord.app.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletRecordRepositoryImpl implements ToiletRecordRepository {

  private final JpaToiletRecordRepository jpa;

  @Override
  public void save(ToiletRecord record) {
    jpa.save(record);
  }

  @Override
  public Optional<ToiletRecord> findByUser_IdAndIdAndIsDeletedFalse(Long userId, Long recordId) {
    return jpa.findByUser_IdAndIdAndIsDeletedFalse(userId, recordId);
  }

  @Override
  public Optional<ToiletRecord> findByIdAndIsDeletedFalse(Long recordId) {
    return jpa.findByIdAndIsDeletedFalse(recordId);
  }
}
