package depromeet.lessonfour.server.toiletrecord.app.repository;

import java.util.Optional;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRecordRepository {

  void save(ToiletRecord record);

  Optional<ToiletRecord> findByUser_IdAndIdAndIsDeletedFalse(Long userId, Long recordId);

  Optional<ToiletRecord> findByIdAndIsDeletedFalse(Long recordId);
}
