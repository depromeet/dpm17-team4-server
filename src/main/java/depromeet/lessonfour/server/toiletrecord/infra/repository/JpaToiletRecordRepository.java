package depromeet.lessonfour.server.toiletrecord.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface JpaToiletRecordRepository extends JpaRepository<ToiletRecord, Long> {}
