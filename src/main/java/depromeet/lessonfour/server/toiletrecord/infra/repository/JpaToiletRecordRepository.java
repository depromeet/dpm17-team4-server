package depromeet.lessonfour.server.toiletrecord.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface JpaToiletRecordRepository extends JpaRepository<ToiletRecord, Long> {}
public interface JpaToiletRecordRepository extends JpaRepository<ToiletRecord, Long> {

  Optional<ToiletRecord> findByUser_IdAndIdAndIsDeletedFalse(Long userId, Long recordId);

  Optional<ToiletRecord> findByIdAndIsDeletedFalse(Long recordId);
}
