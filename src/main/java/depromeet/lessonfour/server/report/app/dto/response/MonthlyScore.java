package depromeet.lessonfour.server.report.app.dto.response;

import java.time.LocalDate;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.vo.monthly.ScoreSummary;

public record MonthlyScore(MonthlyScore.Score best, MonthlyScore.Score worst) {

  public static MonthlyScore from(ScoreSummary scoreSummary) {
    if (scoreSummary == null) {
      return null;
    }

    ToiletScore best = scoreSummary.best();
    ToiletScore worst = scoreSummary.worst();

    if (best == null && worst == null) {
      return null;
    }

    assert best != null;
    assert worst != null;

    Score bestScore = new Score(best.getDate(), best.getScore());
    Score worstScore = new Score(worst.getDate(), worst.getScore());

    return new MonthlyScore(bestScore, worstScore);
  }

  public record Score(LocalDate occurredAt, int score) {}
}
