package depromeet.lessonfour.server.common.infra;

import java.time.LocalDate;

import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public record DailyExistenceProjection(LocalDate date, boolean exists) implements DailyExistence {}
