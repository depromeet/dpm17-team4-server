package depromeet.lessonfour.server.recordquery.app.dto;

import java.time.LocalDate;
import java.util.List;

public record RecordExistenceListResponse(
    LocalDate startDate, LocalDate endDate, List<DailyExistenceResponseDto> results) {}
