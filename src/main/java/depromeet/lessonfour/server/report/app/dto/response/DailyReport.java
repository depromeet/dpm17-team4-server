package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.report.domain.vo.daily.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.daily.DailyToiletReport;

public record DailyReport(
    DailyActivityReport dailyActivityReport, DailyToiletReport dailyToiletReport) {}
