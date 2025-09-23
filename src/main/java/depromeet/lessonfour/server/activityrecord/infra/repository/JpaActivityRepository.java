package depromeet.lessonfour.server.activityrecord.infra.repository;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaActivityRepository extends JpaRepository<Long, ActivityRecord> {

}
