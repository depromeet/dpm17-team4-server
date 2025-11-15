package depromeet.lessonfour.server.report.domain.vo.toilet;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 성공한 배변은 높은 점수로 이어져야 한다.
 *
 * <p>실패한 배변도 통증/시간으로 인한 “나쁜 경험”을 반영해야 한다.
 *
 * <p>색/모양은 건강 척도이므로 성공한 배변에서만 평가해야 한다.
 *
 * <p>극단적인 입력에서도 점수가 0~100 범위에 자연스럽게 매핑되어야 한다.
 *
 * <p>“점수 하나로 사용자 상태를 간단히 설명”해야 하므로 직관적이어야 한다.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ToiletEvaluation {

  // 가중치
  private static final int SUCCESS_WEIGHT = 3;
  private static final int SHAPE_WEIGHT = 2;
  private static final int COLOR_WEIGHT = 3;
  private static final int DURATION_WEIGHT = 2;
  private static final int PAIN_WEIGHT = 5;

  // 기본 점수
  private static final double BASE_SCORE = 60;

  // 성공/실패 점수
  private static final int SUCCESS_BONUS = 5;
  private static final int FAIL_PENALTY = -4;

  // 통증 threshold
  private static final int PAIN_SEVERE_THRESHOLD = 80;
  private static final int PAIN_MODERATE_THRESHOLD = 50;
  private static final int PAIN_MILD_THRESHOLD = 20;

  // 소요시간 threshold
  private static final int DURATION_LONG_THRESHOLD = 10;

  private final double score;
  private final ToiletEvaluationLevel level;
  private final ToiletColor color;
  private final ToiletShape shape;
  private final int duration;
  private final double pain;
  private final String note;
  private final ActivityAt occurredAt;
  private final boolean isSuccess;

  public static DailyToiletReport summarize(List<ToiletRecord> records) {
    List<ToiletEvaluation> evaluations = records.stream().map(ToiletEvaluation::evaluate).toList();

    return DailyToiletReport.summarize(evaluations);
  }

  private static ToiletEvaluation evaluate(ToiletRecord record) {
    double score = BASE_SCORE;

    score += successScore(record.isSuccessful()) * SUCCESS_WEIGHT;
    score += durationPenalty(record.getDuration()) * DURATION_WEIGHT;
    score += painPenalty(record.getPain()) * PAIN_WEIGHT;

    if (record.isSuccessful()) {
      score += record.getColor().getScore() * COLOR_WEIGHT;
      score += record.getShape().getScore() * SHAPE_WEIGHT;
    }

    double finalScore = normalizeScore(score);

    return ToiletEvaluation.builder()
        .score(finalScore)
        .level(ToiletEvaluationLevel.from(finalScore))
        .duration(record.getDuration())
        .color(record.getColor())
        .shape(record.getShape())
        .pain(record.getPain())
        .note(record.getNote())
        .occurredAt(record.getActivityAt())
        .isSuccess(record.isSuccessful())
        .build();
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
      return -5;
    }
    if (pain > PAIN_MODERATE_THRESHOLD) {
      return -3;
    }
    if (pain > PAIN_MILD_THRESHOLD) {
      return -1;
    }
    return 0;
  }

  // 소요시간별 점수
  private static int durationPenalty(int minute) {
    if (minute > DURATION_LONG_THRESHOLD) {
      return -4;
    }
    return 0;
  }

  public boolean failed() {
    return !isSuccess;
  }
}
