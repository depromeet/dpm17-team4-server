package depromeet.lessonfour.server.toiletrecord.app.dto.response;

import java.time.LocalDateTime;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

public record ToiletRecordResponseDto(
    Long id,
    Long userId,
    LocalDateTime occurredAt,
    boolean isSuccessful,
    ToiletColor color,
    ToiletShape shape,
    int pain,
    int duration,
    String note) {
  public static ToiletRecordResponseDto of(ToiletRecord entity) {
    return new ToiletRecordResponseDto(
        entity.getId(),
        entity.getUser().getId(),
        entity.getActivityAt().toDateTime(),
        entity.isSuccessful(),
        entity.getColor(),
        entity.getShape(),
        entity.getPain(),
        entity.getDuration(),
        entity.getNote());
  }
}
