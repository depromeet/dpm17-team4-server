package depromeet.lessonfour.server.report.app.dto.response;

import java.time.LocalDate;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public record MonthlyScore(Score best, Score worst) {

  public static MonthlyScore from(ToiletScore best, ToiletScore worst) {
    if (best == null || worst == null) {
      return null;
    }

    Score bestScore = new Score(best.getDate(), best.getScore());
    Score worstScore = new Score(worst.getDate(), worst.getScore());

    return new MonthlyScore(bestScore, worstScore);
  }

  public record Score(LocalDate occurredAt, int score) {}
}
