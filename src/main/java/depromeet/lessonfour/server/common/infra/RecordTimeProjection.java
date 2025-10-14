package depromeet.lessonfour.server.common.infra;

import java.time.LocalTime;

import depromeet.lessonfour.server.common.domain.view.RecordTimeView;

public record RecordTimeProjection(Long id, LocalTime activityTime) implements RecordTimeView {}
