package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.PooEvaluation;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyPooEvaluator {

  public PooEvaluation evaluate() {
    return new PooEvaluation();
  }
}
