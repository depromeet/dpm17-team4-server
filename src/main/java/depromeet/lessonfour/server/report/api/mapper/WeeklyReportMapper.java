package depromeet.lessonfour.server.report.api.mapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.DefecationScore;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.UserAverage;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WeeklyFoodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WeeklyStressSection;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WeeklyWaterSection;
import depromeet.lessonfour.server.report.api.dto.response.SuggestionSection;
import depromeet.lessonfour.server.report.api.mapper.activity.FoodMapper;
import depromeet.lessonfour.server.report.api.mapper.activity.ReportMapperUtils;
import depromeet.lessonfour.server.report.api.mapper.activity.StressMapper;
import depromeet.lessonfour.server.report.api.mapper.activity.WaterMapper;
import depromeet.lessonfour.server.report.api.mapper.suggestion.SuggestionMapper;
import depromeet.lessonfour.server.report.app.dto.response.WeeklyReport;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WeeklyReportMapper {

  private final StressMapper stressMapper;
  private final FoodMapper foodMapper;
  private final WaterMapper waterMapper;
  private final SuggestionMapper suggestionMapper;

  public GetWeeklyReportResponseDto map(WeeklyReport weeklyReport, LocalDateTime updatedAt) {

    // updatedAt 기준 이번 주 월요일
    LocalDate thisWeekStartDate = updatedAt.toLocalDate().with(DayOfWeek.MONDAY);

    // 배변 점수 매핑
    DefecationScore defecationScore = mapDefecationScore(weeklyReport);

    UserAverage userAverage = mapUserAverage(weeklyReport.thisWeekAverageScore());

    // 생활 기록 매핑
    WeeklyFoodSection foodSection =
        foodMapper.mapWeekly(
            weeklyReport.lastWeekActivity(), weeklyReport.thisWeekActivity(), thisWeekStartDate);

    WeeklyWaterSection waterSection = waterMapper.mapWeekly(weeklyReport.thisWeekActivity());

    WeeklyStressSection stressSection = stressMapper.mapWeekly(weeklyReport.thisWeekActivity());

    // 추천 습관 매핑
    SuggestionSection suggestionSection = suggestionMapper.map(weeklyReport);

    return new GetWeeklyReportResponseDto(
        updatedAt,
        defecationScore,
        userAverage,
        foodSection,
        waterSection,
        stressSection,
        suggestionSection);
  }

  private static DefecationScore mapDefecationScore(WeeklyReport weeklyReport) {
    return new DefecationScore(
        weeklyReport.lastWeekAverageScore(),
        weeklyReport.thisWeekAverageScore(),
        weeklyReport.dailyScores().stream().map(Double::valueOf).toList());
  }

  private static UserAverage mapUserAverage(double thisWeekAverageScore) {
    double me = thisWeekAverageScore;
    double average = 60.0; // TODO: 실제 전체 평균으로 치환 예정
    double topPercent = ReportMapperUtils.estimateTopPercent(me, average);
    return new UserAverage(me, average, topPercent);
  }
}
