package depromeet.lessonfour.server.activityrecord.app.dto.response;

import java.time.LocalDate;

public record DailyExistenceResponse(LocalDate date, boolean exists) {}
