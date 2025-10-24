package depromeet.lessonfour.server.common.infra;

import java.time.LocalTime;

import depromeet.lessonfour.server.common.domain.vo.RecordTime;

public record RecordTimeProjection(Long id, LocalTime activityTime) implements RecordTime {}
