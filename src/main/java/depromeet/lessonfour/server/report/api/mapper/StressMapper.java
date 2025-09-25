package depromeet.lessonfour.server.report.api.mapper;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.StressReport;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;

@Component
public class StressMapper {

  public StressReport map(StressEvaluation stressEvaluation) {
    return switch (stressEvaluation) {
      case VERY_LOW, LOW, MEDIUM -> new StressReport(
          "긍정적인 당신, 칭찬해요!\n그 마인드 오래도록 유지해봐요",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_good.png");
      case HIGH, VERY_HIGH -> new StressReport(
          "스트레스 관리가 필요해요.\n가벼운 산책이나 명상은 어때요?",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_bad.png");
      case NONE -> null;
    };
  }
}
