package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;

public record DailyReport(
    DailyActivityReport dailyActivityReport,
    DailyToiletReport dailyToiletReport,
    Suggestion suggestion) {}
