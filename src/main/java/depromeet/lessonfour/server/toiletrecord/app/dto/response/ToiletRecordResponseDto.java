package depromeet.lessonfour.server.toiletrecord.app.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

public record ToiletRecordResponseDto(
    UUID id,
    Long userId,
    LocalDateTime toiletAt,
    boolean isToiletSuccess,
    ToiletColor toiletColor,
    ToiletShape toiletShape,
    int painScore,
    int toiletDuration,
    String additionalNote) {
  public static ToiletRecordResponseDto of(ToiletRecord entity) {
    return new ToiletRecordResponseDto(
        entity.getId(),
        entity.getUser().getId(),
        entity.getToiletAt(),
        entity.isToiletSuccess(),
        entity.getToiletColor(),
        entity.getToiletShape(),
        entity.getPainScore(),
        entity.getToiletDuration(),
        entity.getAdditionalNote());
  }
}
