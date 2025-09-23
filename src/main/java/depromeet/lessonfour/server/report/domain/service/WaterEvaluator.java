package depromeet.lessonfour.server.report.domain.service;

import depromeet.lessonfour.server.report.domain.vo.ReportPeriod;
import depromeet.lessonfour.server.report.domain.vo.ReportType;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;

@Component
public class WaterEvaluator {

  public ReportType getKey() {
    return ReportType.WATER;
  }

  public WaterEvaluation evaluate(Long userId, ReportPeriod period) {
    return null;
  }
}
