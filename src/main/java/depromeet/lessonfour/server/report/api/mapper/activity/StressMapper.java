package depromeet.lessonfour.server.report.api.mapper.activity;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyStressReport;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyStressSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyStressSection.StressItem;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.activity.StressEvaluation;

@Component
public class StressMapper {

  private record StressMapping(String message, String image) {}

  private static final Map<StressEvaluation, StressMapping> STRESS_MAPPINGS =
      Map.of(
          StressEvaluation.VERY_LOW,
              new StressMapping(
                  "삐용삐용! 스트레스 만땅! 산책이나 명상을 해볼까요?",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_very_bad.png"),
          StressEvaluation.LOW,
              new StressMapping(
                  "스트레스에 잡아먹히지 않도록 조심해봐요!",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_bad.png"),
          StressEvaluation.MEDIUM,
              new StressMapping(
                  "스트레스 발생! 마음을 다스릴 수 있도록 노력해요",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png"),
          StressEvaluation.HIGH,
              new StressMapping(
                  "긍정적인 당신! 그 마인드 오래도록 유지해봐요",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_good.png"),
          StressEvaluation.VERY_HIGH,
              new StressMapping(
                  "스트레스 Zero! 행복한 하루가 되셨군요?",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_very_good.png"));

  public DailyStressReport mapDaily(StressEvaluation stressEvaluation) {
    if (stressEvaluation == StressEvaluation.NONE) {
      return null;
    }

    StressMapping mapped = STRESS_MAPPINGS.get(stressEvaluation);
    return new DailyStressReport(mapped.message, mapped.image);
  }

  public MonthlyStressSection mapStressSection(MonthlyReport report) {
    StressEvaluation monthlyAverageStress = report.getMonthlyAverageStress();

    if (monthlyAverageStress == StressEvaluation.NONE) {
      return MonthlyStressSection.empty();
    }

    // 월 평균 스트레스 기준으로 메시지 생성
    StressMapping mapped = STRESS_MAPPINGS.get(monthlyAverageStress);

    // 주차별 평균 스트레스를 StressItem으로 매핑
    List<StressItem> items =
        report.getWeeklyAverageStress().stream()
            .map(
                weeklyAvg ->
                    new MonthlyStressSection.StressItem(
                        weeklyAvg.weekIndex() + "주차", weeklyAvg.representative().name()))
            .toList();

    return new MonthlyStressSection(mapped.message(), mapped.image(), items);
  }
}
