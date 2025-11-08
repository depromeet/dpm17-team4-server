package depromeet.lessonfour.server.report.api.mapper;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.StressReport;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;

@Component
public class StressMapper {

  public StressReport map(StressEvaluation stressEvaluation) {
    if (stressEvaluation == null || stressEvaluation == StressEvaluation.NONE) {
      return new StressReport(
          "스트레스 기록이 없어요\n마음 편한 하루였네요!",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png");
    }

    return switch (stressEvaluation) {
      case VERY_LOW -> new StressReport(
          "삐용삐용! 스트레스 만땅! 산책이나 명상을 해볼까요?",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_very_bad.png");
      case LOW -> new StressReport(
          "스트레스에 잡아먹히지 않도록 조심해봐요!",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_bad.png");
      case MEDIUM -> new StressReport(
          "스트레스 발생! 마음을 다스릴 수 있도록 노력해요",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png");
      case HIGH -> new StressReport(
          "긍정적인 당신! 그 마인드 오래도록 유지해봐요",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_good.png");
      case VERY_HIGH -> new StressReport(
          "스트레스 Zero! 행복한 하루가 되셨군요?",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_very_good.png");
      case NONE -> throw new IllegalStateException("unreachable");
    };
  }
}
