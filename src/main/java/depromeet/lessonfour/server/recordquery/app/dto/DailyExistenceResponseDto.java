package depromeet.lessonfour.server.recordquery.app.dto;

import java.time.LocalDate;

public record DailyExistenceResponseDto(
    LocalDate date, boolean activityExists, boolean stoolExists) {}
