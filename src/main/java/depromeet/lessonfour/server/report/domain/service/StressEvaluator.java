package depromeet.lessonfour.server.report.domain.service;

import depromeet.lessonfour.server.report.domain.vo.ReportPeriod;
import depromeet.lessonfour.server.report.domain.vo.ReportType;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StressEvaluator {

  public ReportType getKey() {
    return ReportType.STRESS;
  }

  public StressEvaluation evaluate(Long userId, ReportPeriod period) {
    return null;
  }
}
