package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;

@Component
public class DailyWaterEvaluator {

  public WaterEvaluation evaluate() {
    return new WaterEvaluation();
  }
}
