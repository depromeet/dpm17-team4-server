package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.PooEvaluation;

@Component
public class DailyPooEvaluator {

  public PooEvaluation evaluate() {
    return new PooEvaluation();
  }
}
