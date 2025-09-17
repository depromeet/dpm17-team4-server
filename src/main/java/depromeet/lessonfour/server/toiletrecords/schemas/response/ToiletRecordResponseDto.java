package depromeet.lessonfour.server.toiletrecords.schemas.response;

import java.time.LocalDateTime;
import java.util.UUID;

import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletColor;
import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletRecord;
import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletShape;

public record ToiletRecordResponseDto(
    UUID id,
    UUID userId,
    LocalDateTime selectedWhen,
    boolean selectedSuccess,
    ToiletColor selectedColor,
    ToiletShape selectedShape,
    int selectedPain,
    int selectedTimeTaken,
    String selectedOptional) {
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
