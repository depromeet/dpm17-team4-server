package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;

public record DailyReport(
    DailyActivityReport dailyActivityReport, DailyToiletReport dailyToiletReport) {}
