package depromeet.lessonfour.server.report.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public interface JpaToiletScoreRepository extends JpaRepository<ToiletScore, Long> {}
