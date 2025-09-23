package depromeet.lessonfour.server.report.domain.policy;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.PooEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.StoolEvaluation;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

@Component
public class StoolEvaluationPolicy {

  // 가중치
  private static final int SUCCESS_WEIGHT = 5;
  private static final int SHAPE_WEIGHT = 3;
  private static final int COLOR_WEIGHT = 2;
  private static final int DURATION_WEIGHT = 1;
  private static final int PAIN_WEIGHT = 4;

  // 기본 점수
  private static final double BASE_SCORE = 50;

  // 성공/실패 점수
  private static final int SUCCESS_BONUS = 10;
  private static final int FAIL_PENALTY = -20;

  // 통증 threshold
  private static final int PAIN_SEVERE_THRESHOLD = 80;
  private static final int PAIN_MODERATE_THRESHOLD = 50;
  private static final int PAIN_MILD_THRESHOLD = 20;

  // 소요시간 threshold
  private static final int DURATION_LONG_THRESHOLD = 10;

  public StoolEvaluation evaluate(ToiletRecord record) {
    double score = BASE_SCORE;

    score += successScore(record.isSuccessful()) * SUCCESS_WEIGHT;
    score += record.getColor().getScore() * COLOR_WEIGHT;
    score += record.getShape().getScore() * SHAPE_WEIGHT;
    score += durationPenalty(record.getDuration()) * DURATION_WEIGHT;
    score += painPenalty(record.getPain()) * PAIN_WEIGHT;

    double finalScore = normalizeScore(score);

    return StoolEvaluation.from(PooEvaluationLevel.from(finalScore), record);
  }

  private static double normalizeScore(double score) {
    // TODO :  단순하게 cliping 처리, 추후 개선 필요
    return Math.round(Math.max(0, Math.min(100, score)));
  }

  // 성공 점수
  private static int successScore(boolean success) {
    return success ? SUCCESS_BONUS : FAIL_PENALTY;
  }

  // 통증별 점수
  private static int painPenalty(int pain) {
    if (pain > PAIN_SEVERE_THRESHOLD) {
      return -30;
    }
    if (pain > PAIN_MODERATE_THRESHOLD) {
      return -15;
    }
    if (pain > PAIN_MILD_THRESHOLD) {
      return -5;
    }
    return 0;
  }

  // 소요시간별 점수
  private static int durationPenalty(int minute) {
    if (minute > DURATION_LONG_THRESHOLD) {
      return -10;
    }
    return 0;
  }
}
