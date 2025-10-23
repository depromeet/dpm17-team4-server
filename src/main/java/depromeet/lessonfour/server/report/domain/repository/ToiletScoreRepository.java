package depromeet.lessonfour.server.report.domain.repository;

import java.time.LocalDate;
import java.util.Optional;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public interface ToiletScoreRepository {

  int getScoreByActivityAt(Long userId, ActivityAt activityAt);

  Optional<ToiletScore> findByDate(Long userId, LocalDate date);

  ToiletScore save(ToiletScore toiletScore);
}
