package depromeet.lessonfour.server.report.api.mapper.activity;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyFoodReport;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyFoodReportMeal;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.FoodReportItem;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyFoodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyFoodSection.MonthlyComparison;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyFoodSection.WeeklyFoodGroup;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WeeklyFoodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WeeklyFoodSection.FoodItem;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WeeklyFoodSection.WeeklyComparison;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.DayType;
import depromeet.lessonfour.server.report.domain.vo.activity.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.activity.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReportGroup;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FoodMapper {

  private final Clock clock;

  /** 일간 음식 보고서 매핑 */
  public DailyFoodReport mapDaily(List<FoodEvaluation> foodEvaluations) {
    String message = getMessage(foodEvaluations);

    List<FoodReportItem> items =
        foodEvaluations.stream().map(this::createFoodReportItem).collect(toList());

    if (foodEvaluations.size() == 1) {
      FoodEvaluation evaluation = foodEvaluations.getFirst();
      DayType dayType = evaluation.getDayType();

      if (dayType == DayType.YESTERDAY) {
        // 어제 데이터만 있는 경우: 어제 데이터 + 오늘 빈 데이터
        items = List.of(createFoodReportItem(evaluation), createEmptyFoodReportItem(DayType.TODAY));
      } else {
        // 오늘 데이터만 있는 경우: 어제 빈 데이터 + 오늘 데이터
        items =
            List.of(createEmptyFoodReportItem(DayType.YESTERDAY), createFoodReportItem(evaluation));
      }
    }
    // 두 날짜 모두 있는 경우는  DayType 순서로 정렬
    else if (foodEvaluations.size() == 2) {
      items =
          foodEvaluations.stream()
              .sorted(comparing(FoodEvaluation::getDayType))
              .map(this::createFoodReportItem)
              .collect(toList());
    }

    // 데이터가 없는 경우: 어제와 오늘 모두 빈 데이터
    else if (foodEvaluations.isEmpty()) {
      items = List.of();
    }

    return new DailyFoodReport(message, items);
  }

  private FoodReportItem createFoodReportItem(FoodEvaluation foodEvaluation) {
    List<DailyFoodReportMeal> meals =
        foodEvaluation.getFoodsByMealTime().entrySet().stream()
            .map(
                entry ->
                    new DailyFoodReportMeal(
                        entry.getKey(),
                        foodEvaluation.isMealDangerous(entry.getKey()),
                        entry.getValue()))
            .collect(toList());

    return new FoodReportItem(getDate(foodEvaluation.getDayType()), meals);
  }

  private FoodReportItem createEmptyFoodReportItem(DayType dayType) {
    return new FoodReportItem(getDate(dayType), List.of());
  }

  private LocalDateTime getDate(DayType dateType) {
    return dateType == DayType.YESTERDAY
        ? LocalDateTime.now(clock).minusDays(1)
        : LocalDateTime.now(clock);
  }

  private static String getMessage(List<FoodEvaluation> foodEvaluations) {
    final String DANGEROUS_MESSAGE = "맵고 자극적인 음식이 장을 자극했을 수 있어요";
    final String SAFE_MESSAGE = "장에 좋은 음식 잘 선택하셨네요!";

    FoodEvaluation todayEvaluation =
        foodEvaluations.stream()
            .filter(e -> e.getDayType() == DayType.TODAY)
            .findFirst()
            .orElse(null);
    if (foodEvaluations.isEmpty()
        || todayEvaluation == null
        || todayEvaluation.getFoodsByMealTime().isEmpty()) {
      return "음식 기록이 없어요. 장에 좋은 음식을 먹어볼까요?";
    }
    boolean eatDangerousFood = foodEvaluations.stream().anyMatch(FoodEvaluation::isDangerous);

    return eatDangerousFood ? DANGEROUS_MESSAGE : SAFE_MESSAGE;
  }

  /** 주간 음식 섹션 매핑 */
  public WeeklyFoodSection mapWeekly(
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

    return new WeeklyFoodSection(message, comparison, items);
  }

  /** 월간 음식 섹션 매핑 */
  public MonthlyFoodSection mapMonthly(MonthlyReport report) {
    MonthlyActivityReport activityReport = report.activityReport();
    if (activityReport.weeklyActivityReportGroups().isEmpty()) {
      return MonthlyFoodSection.empty();
    }

    List<WeeklyFoodGroup> dtoGroups =
        activityReport.weeklyActivityReportGroups().stream()
            .map(this::toDtoWeeklyFoodGroup)
            .toList();

    int lastMonthDangerous = activityReport.lastMonthDangerousDays();
    int thisMonthDangerous = activityReport.dangerousFoodDays();

    String message = getFoodSectionMessage(thisMonthDangerous);

    return new MonthlyFoodSection(
        message, new MonthlyComparison(lastMonthDangerous, thisMonthDangerous), dtoGroups);
  }

  private static String getFoodSectionMessage(int dangerousFoodDays) {
    if (dangerousFoodDays >= 10) {
      return "자극적인 음식을 10회 이상 섭취했어요\n식단 관리가 필요해요!";
    }

    if (dangerousFoodDays > 0) {
      return "자극적인 음식을 10회 미만으로 섭취했어요.\n지속적으로 줄여나가요!";
    }

    return "건강한 식단을\n열심히 유지하고 계시네요!";
  }

  private WeeklyFoodGroup toDtoWeeklyFoodGroup(WeeklyActivityReportGroup g) {
    String weekLabel = g.weekIndex() + "주차";
    LocalDate weekStart = g.startDate();
    LocalDate weekEnd = g.endDate();

    List<DailyActivityReport> dailyReports = g.dailyReports();

    List<WeeklyFoodGroup.FoodItem> items =
        IntStream.range(0, dailyReports.size())
            .mapToObj(
                dayOffset -> {
                  LocalDate currentDate = weekStart.plusDays(dayOffset);
                  DailyActivityReport dailyReport = dailyReports.get(dayOffset);

                  // 주차 범위를 벗어나면 빈 리스트 반환
                  if (currentDate.isAfter(weekEnd)) {
                    return List.<WeeklyFoodGroup.FoodItem>of();
                  }

                  return dailyReport.getFoodEvaluations().stream()
                      .flatMap(
                          foodEvaluation ->
                              foodEvaluation.getFoodsByMealTime().entrySet().stream()
                                  .filter(
                                      entry ->
                                          entry.getValue() != null && !entry.getValue().isEmpty())
                                  .map(
                                      entry ->
                                          new WeeklyFoodGroup.FoodItem(
                                              currentDate.toString(),
                                              entry.getKey().name(),
                                              entry.getValue())))
                      .toList();
                })
            .flatMap(List::stream)
            .toList();

    return new WeeklyFoodGroup(weekLabel, weekStart.toString(), weekEnd.toString(), items);
  }
}
