package depromeet.lessonfour.server.report.api.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyToiletReportResponse;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.ToiletReportItem;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.ToiletSummary;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletColor;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletColor.ColorCount;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletPain;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletPain.ToiletPainComparison;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletPeriod;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletShape;
import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.app.dto.response.ToiletShapeCount;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPeriodCount;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

@Component
public class ToiletReportMapper {

  // 일간 리포트
  public DailyToiletReportResponse map(DailyToiletReport dailyToiletReport) {
    return DailyMapper.map(dailyToiletReport);
  }

  // 월간 리포트
  public List<MonthlyToiletShape> mapShape(List<ToiletShapeCount> shapeCounts) {
    return shapeCounts.stream().map(MonthlyMapper::fromShape).toList();
  }

  public MonthlyToiletColor mapColor(List<ToiletColorCount> colorCounts) {
    return MonthlyMapper.fromColor(colorCounts);
  }

  public MonthlyToiletPain mapPain(ToiletPainDistribution painDistribution) {
    return MonthlyMapper.fromPain(painDistribution);
  }

  public List<MonthlyToiletPeriod> mapPeriod(List<ToiletPeriodCount> periodCounts) {
    return MonthlyMapper.fromPeriod(periodCounts);
  }

  private static class DailyMapper {

    record HeroCharacter(
        String image, String caption, String message, List<String> backgroundColors) {}

    private static final Map<ToiletEvaluationLevel, HeroCharacter> characterMap =
        Map.of(
            ToiletEvaluationLevel.VERY_BAD,
            new HeroCharacter(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_very_bad.png",
                "화가 잔뜩 난 대장",
                "전문가 상담이 필요해요!",
                List.of("#A4141E", "#FF535F")),
            ToiletEvaluationLevel.BAD,
            new HeroCharacter(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_bad.png",
                "속상한 대장",
                "잠시 관리가 필요해요!",
                List.of("#DD5612", "#F6A85F")),
            ToiletEvaluationLevel.AVERAGE,
            new HeroCharacter(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_medium.png",
                "얌전한 대장",
                "무난한 하루가 되었군요!",
                List.of("#2B42B4", "#8F58FF")),
            ToiletEvaluationLevel.GOOD,
            new HeroCharacter(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_good.png",
                "신이 난 대장",
                "개운하실 것 같아요!",
                List.of("#134DB1", "#588DFF")),
            ToiletEvaluationLevel.VERY_GOOD,
            new HeroCharacter(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/colon_very_good.png",
                "기분 좋은 대장",
                "장 컨디션 아주 굿!",
                List.of("#0C7C30", "#7DD357")));

    private static final String message =
        "전문가의 상담이 필요해요. 복통이 매우 심했다면 단순한 식사 문제를 넘어서 장염이나 자극적인 음식으로 인한 장 트러블일 수 있습니다.";

    static DailyToiletReportResponse map(DailyToiletReport dailyToiletReport) {
      if (dailyToiletReport.getLevel() == ToiletEvaluationLevel.NONE) {
        return null;
      }
      HeroCharacter heroCharacter = characterMap.get(dailyToiletReport.getLevel());
      return new DailyToiletReportResponse(
          dailyToiletReport.getToiletScore(),
          new ToiletSummary(
              heroCharacter.image(),
              heroCharacter.backgroundColors(),
              heroCharacter.caption(),
              heroCharacter.message()),
          dailyToiletReport.getItems().stream()
              .map(
                  item ->
                      new ToiletReportItem(
                          item.getOccurredAt().toDateTime(),
                          message,
                          item.getColor(),
                          item.getShape(),
                          item.getDuration(),
                          item.getPain(),
                          item.getNote()))
              .toList());
    }
  }

  private static class MonthlyMapper {

    private static final Map<ToiletShape, String> MESSAGE_BY_SHAPE =
        Map.of(
            ToiletShape.RABBIT, "변비 주의",
            ToiletShape.ROCK, "",
            ToiletShape.BANANA, "",
            ToiletShape.CORN, "수분 충전 필요",
            ToiletShape.CREAM, "설사 주의",
            ToiletShape.PORRIDGE, "설사 주의",
            ToiletShape.WATER, "설사 주의");

    private static final Map<ToiletColor, String> MESSAGE_BY_COLOR =
        Map.of(
            ToiletColor.RED, "혈변은 건강의 적신호예요. 대장염, 대장암, 치질 등의 문제일 수도 있어요. 빠른 병원 방문을 권장해요.",
            ToiletColor.GRAY, "흰색은 건강의 적신호예요. 간이나 담도가 좋지 않은 상태일 수도 있어요. 빠른 병원 방문을 권장해요.",
            ToiletColor.BLACK, "흑변은 건강의 적신호예요. 위궤양, 위암 등 위 관련 문제일\n" + "수도 있어요. 즉시 병원을 방문하셔야 해요");

    private static final List<ToiletColor> PRIORITY =
        List.of(ToiletColor.RED, ToiletColor.GRAY, ToiletColor.BLACK);

    static MonthlyToiletShape fromShape(ToiletShapeCount shapeCount) {
      return new MonthlyToiletShape(
          shapeCount.shape().getValue(),
          shapeCount.count(),
          MESSAGE_BY_SHAPE.get(shapeCount.shape()));
    }

    static MonthlyToiletColor fromColor(List<ToiletColorCount> toiletColorCounts) {
      List<ToiletColorCount> safe = (toiletColorCounts == null) ? List.of() : toiletColorCounts;

      List<ColorCount> items = safe.stream().map(ColorCount::from).toList();

      String message =
          PRIORITY.stream()
              .filter(
                  priorityColor ->
                      safe.stream().anyMatch(c -> c.color() == priorityColor && c.count() > 0))
              .map(MESSAGE_BY_COLOR::get)
              .findFirst()
              .orElse(null); // 경고 색상이 없으면 메시지 없이

      return new MonthlyToiletColor(items, message);
    }

    static ToiletPainComparison fromPainDiff(int painDiff) {
      if (painDiff > 0) {
        return new ToiletPainComparison("increased", painDiff);
      } else if (painDiff == 0) {
        return new ToiletPainComparison("same", painDiff);
      } else {
        return new ToiletPainComparison("decreased", -painDiff);
      }
    }

    static MonthlyToiletPain fromPain(ToiletPainDistribution toiletPainDistribution) {
      return new MonthlyToiletPain(
          toiletPainDistribution.veryLow(),
          toiletPainDistribution.low(),
          toiletPainDistribution.medium(),
          toiletPainDistribution.high(),
          toiletPainDistribution.veryHigh(),
          fromPainDiff(toiletPainDistribution.painDiff()));
    }

    static List<MonthlyToiletPeriod> fromPeriod(List<ToiletPeriodCount> periodCounts) {
      return periodCounts.stream()
          .map(
              periodCount ->
                  new MonthlyToiletPeriod(periodCount.period().getValue(), periodCount.count()))
          .toList();
    }
  }
}
