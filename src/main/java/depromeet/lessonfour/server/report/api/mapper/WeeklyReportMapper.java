package depromeet.lessonfour.server.report.api.mapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.DefecationScore;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.FoodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.FoodSection.FoodItem;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.FoodSection.WeeklyComparison;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.StressSection;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.StressSection.StressItem;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.SuggestionSection;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.SuggestionSection.SuggestionItem;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.UserAverage;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WaterSection;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WaterSection.WaterItem;
import depromeet.lessonfour.server.report.app.dto.response.WeeklyReport;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WeeklyReportMapper {

  private final StressMapper stressMapper;
  private final SuggestionMapper suggestionMapper;

  public GetWeeklyReportResponseDto map(WeeklyReport weeklyReport, LocalDateTime updatedAt) {

    // updatedAt 기준 이번 주 월요일
    LocalDate thisWeekStartDate = updatedAt.toLocalDate().with(DayOfWeek.MONDAY);

    DefecationScore defecationScore =
        new DefecationScore(
            weeklyReport.lastWeekAverageScore(),
            weeklyReport.thisWeekAverageScore(),
            weeklyReport.dailyScores().stream().map(Double::valueOf).toList());

    UserAverage userAverage = mapUserAverage(weeklyReport.thisWeekAverageScore());

    FoodSection foodSection =
        mapFoodSection(
            weeklyReport.lastWeekActivity(), weeklyReport.thisWeekActivity(), thisWeekStartDate);

    WaterSection waterSection = mapWaterSection(weeklyReport.thisWeekActivity());

    StressSection stressSection = mapStressSection(weeklyReport.thisWeekActivity());

    SuggestionSection suggestionSection = mapSuggestionSection(weeklyReport.suggestion());

    return new GetWeeklyReportResponseDto(
        updatedAt,
        defecationScore,
        userAverage,
        foodSection,
        waterSection,
        stressSection,
        suggestionSection);
  }

  private static UserAverage mapUserAverage(double thisWeekAverageScore) {
    double me = thisWeekAverageScore;
    double average = 60.0; // TODO: 실제 전체 평균으로 치환 예정
    double topPercent = ReportMapperUtils.estimateTopPercent(me, average);
    return new UserAverage(me, average, topPercent);
  }

  private static FoodSection mapFoodSection(
      WeeklyActivityReport lastWeek, WeeklyActivityReport thisWeek, LocalDate thisWeekStartDate) {

    int lastWeekDangerous = (int) lastWeek.dangerousFoodDays();
    int thisWeekDangerous = (int) thisWeek.dangerousFoodDays();

    String message;
    if (thisWeekDangerous >= 10) {
      message = "자극적인 음식을 10회 이상 섭취했어요\n식단 관리가 필요해요!";
    } else if (thisWeekDangerous > 0) {
      message = "자극적인 음식을 10회 미만으로 섭취했어요.\n지속적으로 줄여나가요!";
    } else {
      message = "건강한 식단을\n열심히 유지하고 계시네요!";
    }

    WeeklyComparison comparison = new WeeklyComparison(lastWeekDangerous, thisWeekDangerous);

    // 주간 아이템: 이번 주 DailyActivityReport 기준으로 쭉 펼치기
    List<FoodItem> items = new ArrayList<>();

    int dayIndex = 0;
    for (DailyActivityReport daily : thisWeek.getDailyReports()) {
      LocalDate date = thisWeekStartDate.plusDays(dayIndex);
      for (FoodEvaluation eval :
          (daily.getFoodEvaluations() == null
              ? List.<FoodEvaluation>of()
              : daily.getFoodEvaluations())) {
        eval.getFoodsByMealTime()
            .forEach(
                (mealTime, foods) -> {
                  if (foods != null && !foods.isEmpty()) {
                    items.add(new FoodItem(date.toString(), mealTime.name(), foods));
                  }
                });
      }

      dayIndex++;
    }

    return new FoodSection(message, comparison, items);
  }

  private static WaterSection mapWaterSection(WeeklyActivityReport thisWeek) {
    // WaterLevel → 대략적인 ml 값
    double highValue = 2000.0;
    double mediumValue = 1200.0;
    double lowValue = 600.0;

    List<WaterItem> items = new ArrayList<>();

    String[] dayNames = {
      "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    };

    int idx = 0;
    for (DailyActivityReport daily : thisWeek.getDailyReports()) {
      WaterLevel level =
          daily.getWaterEvaluations().isEmpty()
              ? WaterLevel.NONE
              : daily.getWaterEvaluations().get(0).getLevel();

      double value =
          switch (level) {
            case HIGH -> highValue;
            case MEDIUM -> mediumValue;
            case LOW -> lowValue;
            case NONE -> 0.0;
          };

      String name = idx < dayNames.length ? dayNames[idx] : "DAY_" + (idx + 1);
      items.add(new WaterItem(name, value));
      idx++;
    }

    // 메시지: 가장 높은 수준의 WaterLevel 기준
    WaterLevel maxLevel = WaterLevel.NONE;
    for (DailyActivityReport daily : thisWeek.getDailyReports()) {
      for (WaterEvaluation eval : daily.getWaterEvaluations()) {
        if (eval.getLevel().ordinal() > maxLevel.ordinal()) {
          maxLevel = eval.getLevel();
        }
      }
    }

    String message;
    if (thisWeek.getDailyReports().isEmpty() || maxLevel == WaterLevel.NONE) {
      message = "물 섭취 기록이 없어요\n물을 자주 마셔주세요";
    } else if (maxLevel == WaterLevel.LOW) {
      message = "장이 말라가고 있어요!\n물 섭취량을 늘려야 해요";
    } else {
      message = "물을 잘 섭취하고 계시군요!\n앞으로도 잘 유지해봐요";
    }

    return new WaterSection(message, items);
  }

  private StressSection mapStressSection(WeeklyActivityReport thisWeek) {
    // 1) 일단 items 리스트 만들고
    List<StressItem> items = new ArrayList<>();

    // 2) 아예 주간 활동이 없는 경우 (null or empty) → 기본 메시지 + 빈 리스트
    if (thisWeek == null
        || thisWeek.getDailyReports() == null
        || thisWeek.getDailyReports().isEmpty()) {

      return new StressSection(
          "스트레스 기록이 없어요\n오늘부터 간단히 남겨봐요!",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png",
          items);
    }

    // 3) 주간 최악 스트레스 레벨 계산
    StressEvaluation worst =
        thisWeek.getDailyReports().stream()
            .map(DailyActivityReport::getStressEvaluation)
            .filter(s -> s != null && s != StressEvaluation.NONE)
            // NOTE: enum 선언이 VERY_LOW(최악) → ... → VERY_HIGH(최상), NONE(제외)
            // 낮을수록(ordinal 작을수록) 더 나쁜 상태이므로 min이 "최악"을 의미한다.
            .min(Comparator.comparingInt(Enum::ordinal))
            .orElse(StressEvaluation.NONE);

    // 4) 기존 StressMapper 사용하되, null 이면 기본값으로 보정
    GetDailyReportResponseDto.StressReport base = stressMapper.map(worst);

    if (base == null) {
      // NONE 등에 대해 StressMapper가 null을 주는 경우를 위한 안전장치
      base =
          new GetDailyReportResponseDto.StressReport(
              "스트레스 기록이 없어요\n오늘부터 간단히 남겨봐요!",
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png");
    }

    // 5) 일별 아이템 생성 (요일 라벨은 원하는 대로)
    String[] dayNames = {
      "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    };

    int idx = 0;
    for (DailyActivityReport daily : thisWeek.getDailyReports()) {
      String day = idx < dayNames.length ? dayNames[idx] : "DAY_" + (idx + 1);
      StressEvaluation ev = daily.getStressEvaluation();
      String label = (ev == null ? StressEvaluation.NONE : ev).name(); // NULL-SAFE
      items.add(new StressItem(day, label));
      idx++;
    }

    // 6) 최종 섹션 반환
    return new StressSection(base.message(), base.image(), items);
  }

  private SuggestionSection mapSuggestionSection(Suggestion suggestion) {
    // 기존 SuggestionMapper → Daily용 DTO를 Weekly용으로 재래핑
    var dto = suggestionMapper.map(suggestion);

    List<SuggestionItem> items =
        dto.items().stream()
            .map(i -> new SuggestionItem(i.image(), i.title(), i.content()))
            .toList();

    return new SuggestionSection(dto.message(), items);
  }
}
