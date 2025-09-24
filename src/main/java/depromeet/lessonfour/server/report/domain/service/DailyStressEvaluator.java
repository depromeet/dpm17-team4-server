package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;

@Component
public class DailyStressEvaluator {

  public StressEvaluation evaluate() {
    return new StressEvaluation();
  }
}
