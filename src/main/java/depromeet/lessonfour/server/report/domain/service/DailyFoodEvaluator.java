package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;

@Component
public class DailyFoodEvaluator {

  public FoodEvaluation evaluate() {
    return new FoodEvaluation();
  }
}
