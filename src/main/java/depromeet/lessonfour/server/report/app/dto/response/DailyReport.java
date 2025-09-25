package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;

public record DailyReport(
    ActivityReport activityReport, StoolReport stoolReport, Suggestion suggestion) {}
