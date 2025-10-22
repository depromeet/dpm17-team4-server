package depromeet.lessonfour.server.report.infra;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public interface JpaToiletScoreRepository extends JpaRepository<ToiletScore, Long> {

  Optional<ToiletScore> findByUserIdAndDate(Long userId, LocalDate date);
}
