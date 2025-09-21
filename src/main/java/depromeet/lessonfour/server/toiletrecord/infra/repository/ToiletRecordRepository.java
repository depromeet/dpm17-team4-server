package depromeet.lessonfour.server.toiletrecord.infra.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRecordRepository extends JpaRepository<ToiletRecord, UUID> {}
