package depromeet.lessonfour.server.report.app.dto.response;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;

public record WeeklyReport(
    double lastWeekAverageScore,
    double thisWeekAverageScore,
    List<Integer> dailyScores, // 이번 주 월~일 점수
    WeeklyActivityReport lastWeekActivity, // 지난 주 액티비티 요약
    WeeklyActivityReport thisWeekActivity, // 이번 주 액티비티 요약
    Suggestion suggestion // 주간 습관 제안
    ) {}
