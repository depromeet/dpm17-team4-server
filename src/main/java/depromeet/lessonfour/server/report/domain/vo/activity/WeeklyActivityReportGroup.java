package depromeet.lessonfour.server.report.domain.vo.activity;

import java.time.LocalDate;
import java.util.List;

public record WeeklyActivityReportGroup(
    int weekIndex, // 1~5
    LocalDate startDate, // yyyy-MM-dd
    LocalDate endDate, // yyyy-MM-dd
    List<DailyActivityReport> dailyReports) {}
