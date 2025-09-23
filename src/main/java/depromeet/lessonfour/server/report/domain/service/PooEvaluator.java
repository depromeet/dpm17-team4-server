package depromeet.lessonfour.server.report.domain.service;

import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.PooEvaluation;
import depromeet.lessonfour.server.report.domain.vo.ReportPeriod;
import depromeet.lessonfour.server.report.domain.vo.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PooEvaluator {

  public ReportType getKey() {
    return ReportType.POO;
  }

  public PooEvaluation evaluate(Long userId, ReportPeriod period) {
    return null;
  }

  private FoodEvaluation calculateDaily(Long userId, ReportPeriod period) {
    return null;
  }
}
