package depromeet.lessonfour.server.report.domain.service;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.ReportPeriod;
import depromeet.lessonfour.server.report.domain.vo.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FoodEvaluator {

  private final FoodRepository foodRepository;

  public ReportType getKey() {
    return ReportType.FOOD;
  }

  public FoodEvaluation evaluate(Long userId, ReportPeriod period) {
    switch (period.type()) {
      case DAILY -> {
        return calculateDaily(userId, period);
      }
      default -> {
        log.info("지원하지 않는 ReportPeriod Type 입니다. periodType: {}", period.type());
        throw new ServerException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    }
  }

  private FoodEvaluation calculateDaily(Long userId, ReportPeriod period) {
    return null;
  }
}
