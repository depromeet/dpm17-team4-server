package depromeet.lessonfour.server.report.app.dto.response;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;

public record MonthlyReport(
    RecordCounts recordCounts,
    MonthlyScore monthlyScore,
    List<ToiletShapeCount> shape,
    ToiletTimeDistribution timeDistribution,
    List<ToiletColorCount> color,
    ToiletPainDistribution pain,
    List<ToiletPeriodCount> timeOfDay,
    Suggestion suggestion) {}
