package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.ToiletReport;

public record DailyReport(
    ActivityReport activityReport, ToiletReport toiletReport, Suggestion suggestion) {}
