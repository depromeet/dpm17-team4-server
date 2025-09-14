package depromeet.lessonfour.server.activityrecords.adapters;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.activityrecords.domain.entities.ActivityRecord;

public interface ActivityRecordsRepository extends JpaRepository<ActivityRecord, UUID> {}
