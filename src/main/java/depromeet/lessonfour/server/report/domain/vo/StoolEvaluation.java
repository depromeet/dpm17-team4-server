package depromeet.lessonfour.server.report.domain.vo;

import java.time.LocalDateTime;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class StoolEvaluation {
  private final double score;
  private final StoolEvaluationLevel level;
  private final ToiletColor color;
  private final ToiletShape shape;
  private final int duration;
  private final double pain;
  private final String note;
  private final LocalDateTime occurredAt;

  public static StoolEvaluation from(
      double score, StoolEvaluationLevel level, ToiletRecord record) {
    return StoolEvaluation.builder()
        .score(score)
        .level(level)
        .color(record.getColor())
        .shape(record.getShape())
        .duration(record.getDuration())
        .pain(record.getPain())
        .note(record.getNote())
        .occurredAt(record.getOccurredAt())
        .build();
  }
}
