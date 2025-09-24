package depromeet.lessonfour.server.report.api.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.StoolDailyReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.StoolReportItem;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.StoolSummary;
import depromeet.lessonfour.server.report.domain.vo.StoolEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
public class StoolMapper {

  private static final Map<StoolEvaluationLevel, HeroCharacter> characterMap =
      Map.of(
          StoolEvaluationLevel.VERY_BAD,
              new HeroCharacter(
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_very_bad.png",
                  "화가 잔뜩 난 대장",
                  "전문가 상담이 필요해요!",
                  List.of("#A4141E", "#FF535F")),
          StoolEvaluationLevel.BAD,
              new HeroCharacter(
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_bad.png",
                  "속상한 대장",
                  "잠시 관리가 필요해요!",
                  List.of("#DD5612", "#F6A85F")),
          StoolEvaluationLevel.AVERAGE,
              new HeroCharacter(
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_medium.png",
                  "얌전한 대장",
                  "무난한 하루가 되었군요!",
                  List.of("#2B42B4", "#8F58FF")),
          StoolEvaluationLevel.GOOD,
              new HeroCharacter(
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_good.png",
                  "신이 난 대장",
                  "개운하실 것 같아요!",
                  List.of("##134DB1", "##588DFF")),
          StoolEvaluationLevel.VERY_GOOD,
              new HeroCharacter(
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_very_good.png",
                  "기분 좋은 대장",
                  "장 컨디션 아주 굿!",
                  List.of("##0C7C30", "##7DD357")));

  private String message = "전문가의 상담이 필요해요. 복통이 매우 심했다면 단순한 식사 문제를 넘어서 장염이나 자극적인";

  public StoolDailyReport map(StoolReport stoolReport) {
    // 결과에 따라 score, summary, items mapping 필요
    HeroCharacter heroCharacter = characterMap.get(stoolReport.getLevel());
    return new StoolDailyReport(
        stoolReport.getStoolScore(),
        new StoolSummary(
            heroCharacter.getImage(),
            heroCharacter.getBackgroundColors(),
            heroCharacter.getCaption(),
            heroCharacter.getMessage()),
        stoolReport.getItems().stream()
            .map(
                item ->
                    new StoolReportItem(
                        item.getOccurredAt(),
                        message,
                        item.getColor(),
                        item.getShape(),
                        item.getDuration(),
                        item.getPain(),
                        item.getNote()))
            .toList());
  }

  @Getter
  @AllArgsConstructor
  static class HeroCharacter {
    private final String image;
    private final String caption;
    private final String message;
    private final List<String> backgroundColors;
  }
}
