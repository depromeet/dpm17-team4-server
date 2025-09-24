package depromeet.lessonfour.server.report.api.mapper;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.FoodDailyReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.FoodReportItem;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.FoodReportMeal;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FoodMapper {

  private static final String DANGEROUS_MESSAGE = "맵고 자극적인 음식이 장을 자극했을 수 있어요";
  private static final String SAFE_MESSAGE = "장에 좋은 음식 잘 선택하셨네요!";

  private final Clock clock;

  public FoodDailyReport map(List<FoodEvaluation> foodEvaluations) {
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
      items =
          List.of(
              createEmptyFoodReportItem(DayType.YESTERDAY),
              createEmptyFoodReportItem(DayType.TODAY));
    }

    return new GetDailyReportResponseDto.FoodDailyReport(message, items);
  }

  private FoodReportItem createFoodReportItem(FoodEvaluation foodEvaluation) {
    List<FoodReportMeal> meals =
        foodEvaluation.getFoodsByMealTime().entrySet().stream()
            .map(
                entry ->
                    new FoodReportMeal(
                        entry.getKey(), foodEvaluation.isDangerous(), entry.getValue()))
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
    boolean eatDangerousFood = foodEvaluations.stream().anyMatch(FoodEvaluation::isDangerous);

    return eatDangerousFood ? DANGEROUS_MESSAGE : SAFE_MESSAGE;
  }
}
