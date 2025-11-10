package depromeet.lessonfour.server.report.api.mapper;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyColorSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyFoodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPainSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPeriodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyRecordCount;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyShapeSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyStressSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyTimeDistributionSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyWaterSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.UserAverage;
import depromeet.lessonfour.server.report.api.dto.response.SuggestionSection;
import depromeet.lessonfour.server.report.api.mapper.activity.FoodMapper;
import depromeet.lessonfour.server.report.api.mapper.activity.ReportMapperUtils;
import depromeet.lessonfour.server.report.api.mapper.activity.StressMapper;
import depromeet.lessonfour.server.report.api.mapper.activity.WaterMapper;
import depromeet.lessonfour.server.report.api.mapper.suggestion.SuggestionMapper;
import depromeet.lessonfour.server.report.api.mapper.toilet.ToiletReportMapper;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyScore;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MonthlyReportMapper {

  private final ToiletReportMapper toiletReportMapper;
  private final StressMapper stressMapper;
  private final FoodMapper foodMapper;
  private final WaterMapper waterMapper;
  private final SuggestionMapper suggestionMapper;

  // 컨트롤러 기본 사용
  public GetMonthlyReportResponseDto map(MonthlyReport report) {
    return internalMap(report, null);
  }

  // YearMonth까지 넘기고 싶을 때 사용
  public GetMonthlyReportResponseDto map(MonthlyReport report, YearMonth month) {
    return internalMap(report, month);
  }

  private GetMonthlyReportResponseDto internalMap(MonthlyReport report, YearMonth month) {
    // 통합
    MonthlyRecordCount recordCount = mapRecordCount(report);

    // 배변 점수
    List<Integer> monthlyDefecationScore = report.getWeeklyAverageScore();
    UserAverage userAverage = mapUserAverage(report.getAverageScore());
    MonthlyScore monthlyScore =
        MonthlyScore.from(report.getMaxToiletScore(), report.getMinToiletScore());

    // 배변 기록
    MonthlyShapeSection shapeSection = toiletReportMapper.mapShape(report);
    MonthlyTimeDistributionSection timeSection = toiletReportMapper.mapTimeDistribution(report);
    MonthlyColorSection colorSection = toiletReportMapper.mapColor(report);
    MonthlyPainSection painSection = toiletReportMapper.mapPain(report);
    MonthlyPeriodSection periodSection = toiletReportMapper.mapPeriodSection(report);

    // 생활 기록
    MonthlyFoodSection foodSection = foodMapper.mapMonthly(report);
    MonthlyWaterSection waterSection = waterMapper.mapMonthly(report);
    MonthlyStressSection stressSection = stressMapper.mapMonthly(report);

    // 추천 습관
    SuggestionSection suggestionSection = suggestionMapper.map(report);

    return new GetMonthlyReportResponseDto(
        recordCount,
        monthlyDefecationScore,
        userAverage,
        monthlyScore,
        shapeSection,
        timeSection,
        colorSection,
        painSection,
        periodSection,
        foodSection,
        waterSection,
        stressSection,
        suggestionSection);
  }

  // ===== 상단 =====
  private static MonthlyRecordCount mapRecordCount(MonthlyReport report) {
    return new MonthlyRecordCount(
        report.getTotalCount(), report.getToiletRecordCount(), report.getActivityRecordCount());
  }

  private static UserAverage mapUserAverage(double myAverage) {
    double overallAvg = 60.0; // 전체 평균 미정 → 우선 주간과 동일값
    double topPercent = ReportMapperUtils.estimateTopPercent(myAverage, overallAvg);
    String title = String.format("이번 달 배변 점수는\n 꾸룩 사용자 중 상위 %.0f%%예요", topPercent);
    return new UserAverage(myAverage, overallAvg, topPercent, title);
  }
}
