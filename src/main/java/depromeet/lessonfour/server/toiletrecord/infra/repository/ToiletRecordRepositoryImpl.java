package depromeet.lessonfour.server.toiletrecord.infra.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.toiletrecord.app.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletRecordRepositoryImpl implements ToiletRecordRepository {

  private final JpaToiletRecordRepository jpa;
  private final QueryDslToiletRecordRepository query;

  @Override
  public void save(ToiletRecord record) {
    jpa.save(record);
  }

  @Override
  public Optional<ToiletRecord> findByUserAndId(Long userId, Long recordId) {
    return jpa.findByUser_IdAndIdAndIsDeletedFalse(userId, recordId);
  }

  @Override
  public Optional<ToiletRecord> findByIdAndIsDeletedFalse(Long recordId) {
    return jpa.findByIdAndIsDeletedFalse(recordId);
  }

  @Override
  public List<ToiletRecord> findByDate(Long userId, LocalDate date) {
    return query.findByDate(userId, date);
  }
}
