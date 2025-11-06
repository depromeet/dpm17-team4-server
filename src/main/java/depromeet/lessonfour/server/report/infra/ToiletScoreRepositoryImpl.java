package depromeet.lessonfour.server.report.infra;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletScoreRepositoryImpl implements ToiletScoreRepository {

  private final ToiletScoreQuery query;
  private final JpaToiletScoreRepository jpa;

  @Override
  public int getScoreByActivityAt(Long userId, ActivityAt date) {
    return query.getScoreByActivityAt(userId, date.toDate());
  }

  @Override
  public Optional<ToiletScore> findByDate(Long userId, LocalDate date) {
    return jpa.findByUserIdAndDate(userId, date);
  }

  @Override
  public ToiletScore save(ToiletScore toiletScore) {
    return jpa.save(toiletScore);
  }

  @Override
  public Optional<ToiletScore> findMaxScoreBetween(Long userId, ActivityAt start, ActivityAt end) {
    return query.findMaxScoreBetween(userId, start, end);
  }

  @Override
  public Optional<ToiletScore> findMinScoreBetween(Long userId, ActivityAt start, ActivityAt end) {
    return query.findMinScoreBetween(userId, start, end);
  }
}
