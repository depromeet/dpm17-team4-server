package depromeet.lessonfour.server.report.domain.vo;

import java.time.LocalDateTime;

public record ReportPeriod(LocalDateTime start, LocalDateTime end, PeriodType type) {

}
