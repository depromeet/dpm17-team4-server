package depromeet.lessonfour.server.toiletrecords.schemas.response;

import java.time.LocalDateTime;
import java.util.UUID;

import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletColor;
import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletRecord;
import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletShape;

public record ToiletRecordResponseDto(
    UUID id,
    UUID userId,
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
