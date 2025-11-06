package depromeet.lessonfour.server.report.domain.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.report.domain.policy.FoodEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.policy.StressEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.policy.WaterEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityEvaluationService {

  private final FoodEvaluationPolicy foodEvaluationPolicy;
  private final WaterEvaluationPolicy waterEvaluationPolicy;
  private final StressEvaluationPolicy stressEvaluationPolicy;

  public DailyActivityReport evaluate(ActivityRecord previous, ActivityRecord current) {
    List<FoodEvaluation> foodEvaluations = new ArrayList<>();
    List<WaterEvaluation> waterEvaluations = new ArrayList<>();
    StressEvaluation stressEvaluation = StressEvaluation.empty();

    if (previous != null) {
      foodEvaluations.add(
          foodEvaluationPolicy.calculate(previous.getFoodRecords(), DayType.YESTERDAY));
      waterEvaluations.add(
          waterEvaluationPolicy.calculate(previous.getWaterIntakeCups(), DayType.YESTERDAY));
    }

    if (current != null) {
      foodEvaluations.add(foodEvaluationPolicy.calculate(current.getFoodRecords(), DayType.TODAY));
      waterEvaluations.add(
          waterEvaluationPolicy.calculate(current.getWaterIntakeCups(), DayType.TODAY));
      stressEvaluation = stressEvaluationPolicy.calculate(current.getStressLevel());
    }

    return new DailyActivityReport(foodEvaluations, waterEvaluations, stressEvaluation);
  }
}
