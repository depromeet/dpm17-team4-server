package depromeet.lessonfour.server.toiletrecord.infra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletRecordRepositoryImpl implements ToiletRecordRepository {

  private final JpaToiletRecordRepository jpa;
  private final ToiletRecordQuery query;

  @Override
  public void save(ToiletRecord record) {
    jpa.save(record);
  }

  @Override
  public Optional<ToiletRecord> findById(Long userId, Long recordId) {
    return jpa.findByUser_IdAndIdAndIsDeletedFalse(userId, recordId);
  }

  @Override
  public List<ToiletRecord> findByActivityAt(Long userId, ActivityAt at) {
    return query.findByDate(userId, at);
  }

  @Override
  public List<DailyExistence> existsByActivityAt(Long userId, ActivityAt start, ActivityAt end) {
    return query.findDailyExistencesByActivityAt(userId, start, end);
  }
}
