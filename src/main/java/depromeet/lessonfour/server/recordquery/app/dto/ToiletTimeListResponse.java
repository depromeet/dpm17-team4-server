package depromeet.lessonfour.server.recordquery.app.dto;

import java.time.LocalDate;
import java.util.List;

public record ToiletTimeListResponse(LocalDate date, List<ToiletTimeItemDto> items) {}
