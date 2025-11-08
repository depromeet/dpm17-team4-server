package depromeet.lessonfour.server.report.app.dto.response;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;

public record MonthlyReport(
    RecordCounts recordCounts,
    MonthlyScore monthlyScore,
    double averageScore,
    List<Integer> weeklyAverageScores,
    List<ToiletShapeCount> shape,
    ToiletTimeDistribution timeDistribution,
    List<ToiletColorCount> color,
    ToiletPainDistribution pain,
    List<ToiletPeriodCount> timeOfDay,
    Suggestion suggestion,
    // 월간 Activity 원본 (주차 그룹 포함)
    MonthlyActivityReport activityReport) {}
