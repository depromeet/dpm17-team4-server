package depromeet.lessonfour.server.report.domain.policy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.domain.entity.FoodRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;

@Component
public class FoodEvaluationPolicy {

  private static final int DEFAULT_DANGEROUS_THRESHOLD = 80;

  public FoodEvaluation calculate(List<FoodRecord> records) {
    boolean dangerous = isDangerousExists(records);

    Map<MealTime, List<String>> foodsByMealTime =
        records.stream()
            .collect(
                Collectors.groupingBy(
                    FoodRecord::getMealTime,
                    Collectors.mapping(record -> record.getFood().getName(), Collectors.toList())));

    return new FoodEvaluation(dangerous, foodsByMealTime);
  }

  private static boolean isDangerousExists(List<FoodRecord> records) {
    return records.stream()
        .anyMatch(record -> record.getFood().isDangerous(DEFAULT_DANGEROUS_THRESHOLD));
  }
}
