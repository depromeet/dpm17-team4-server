package depromeet.lessonfour.server.toiletrecord.app.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRecordRepository {

  void save(ToiletRecord record);

  Optional<ToiletRecord> findByUserAndId(Long userId, Long recordId);

  Optional<ToiletRecord> findByIdAndIsDeletedFalse(Long recordId);

  List<ToiletRecord> findByDate(Long userId, LocalDate date);
}
