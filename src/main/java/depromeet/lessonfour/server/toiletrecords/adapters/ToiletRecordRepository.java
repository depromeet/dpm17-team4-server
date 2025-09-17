package depromeet.lessonfour.server.toiletrecords.adapters;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletRecord;

public interface ToiletRecordRepository extends JpaRepository<ToiletRecord, UUID> {
}
