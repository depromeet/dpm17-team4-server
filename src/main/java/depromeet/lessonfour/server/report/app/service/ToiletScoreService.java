package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletScoreService {

  private final ToiletScoreRepository toiletScoreRepository;

  @Transactional(readOnly = true)
  public int getScoreByActivityAt(Long userId, ActivityAt date) {
    return toiletScoreRepository.getScoreByActivityAt(userId, date);
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
