package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import depromeet.lessonfour.server.report.domain.vo.monthly.ScoreSummary;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletScoreService {

  private final ToiletScoreRepository toiletScoreRepository;

  @Transactional(readOnly = true)
  public int getScoreByActivityAt(Long userId, ActivityAt date) {
    return toiletScoreRepository.getScoreByActivityAt(userId, date);
  }

  @Transactional(readOnly = true)
  public ScoreSummary getMonthlyScoreSummary(Long userId, ActivityAt start, ActivityAt end) {
    Optional<ToiletScore> bestOpt = toiletScoreRepository.findMaxScoreBetween(userId, start, end);
    Optional<ToiletScore> worstOpt = toiletScoreRepository.findMinScoreBetween(userId, start, end);

    return new ScoreSummary(bestOpt.orElse(null), worstOpt.orElse(null));
  }

  @Transactional
  public void updateScore(Long userId, int score, ActivityAt activityAt) {
    LocalDate date = activityAt.toDate();

    toiletScoreRepository
        .findByDate(userId, date)
        .ifPresentOrElse(
            existing -> existing.updateScore(score),
            () -> toiletScoreRepository.save(ToiletScore.of(userId, score, date)));
  }
}
