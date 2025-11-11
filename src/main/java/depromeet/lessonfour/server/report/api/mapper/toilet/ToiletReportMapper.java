package depromeet.lessonfour.server.report.api.mapper.toilet;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyToiletReportDto;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.ToiletReportItem;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.ToiletSummary;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.ColorCount;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyColorSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPainSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPainSection.ToiletPainComparison;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPeriodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPeriodSection.MonthlyToiletPeriod;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyShapeSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyTimeDistributionSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletShape;
import depromeet.lessonfour.server.report.domain.vo.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletColorCount;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletTimeDistribution;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ToiletReportMapper {

  // 일간 리포트
  public DailyToiletReportDto mapDaily(DailyToiletReport dailyToiletReport) {
    return DailyMapper.map(dailyToiletReport);
  }

  public record ToiletHeroAssets(String image, List<String> backgroundColors) {}

  public ToiletHeroAssets heroAssetsByLevel(ToiletEvaluationLevel level) {
    var hero = DailyMapper.lookupHero(level); // 내부 맵 재사용
    return new ToiletHeroAssets(hero.image(), hero.backgroundColors());
  }

  /** 배변 모양 섹션 매핑 */
  public MonthlyShapeSection mapShape(MonthlyReport report) {
    final String titleMessage = "이번 달 자주 본 배변 모양이에요";
    final Map<ToiletShape, String> MESSAGE_BY_SHAPE =
        Map.of(
            ToiletShape.RABBIT, "변비 주의",
            ToiletShape.BANANA, "",
            ToiletShape.CORN, "수분 충전 필요",
            ToiletShape.CREAM, "설사 주의",
            ToiletShape.PORRIDGE, "설사 주의",
            ToiletShape.WATER, "설사 주의");

    List<MonthlyToiletShape> items =
        report.getMostFrequentToiletShapes().stream()
            .map(
                shape ->
                    new MonthlyToiletShape(
                        shape.shape().name(),
                        shape.count(),
                        MESSAGE_BY_SHAPE.getOrDefault(shape.shape(), "")))
            .toList();

    return new MonthlyShapeSection(titleMessage, items);
  }

  /** 배변 색상 섹션 매핑 */
  public MonthlyColorSection mapColor(MonthlyReport report) {
    final Map<ToiletColor, String> COLOR_MESSAGE_MAP =
        Map.of(
            ToiletColor.RED, "혈변은 건강의 적신호예요. 대장염, 대장암, 치질 등의 문제일 수도 있어요. 빠른 병원 방문을 권장해요.",
            ToiletColor.WHITE, "흰색은 건강의 적신호예요. 간이나 담도가 좋지 않은 상태일 수도 있어요. 빠른 병원 방문을 권장해요.",
            ToiletColor.BLACK, "흑변은 건강의 적신호예요. 위궤양, 위암 등 위 관련 문제일 수도 있어요. 즉시 병원을 방문하셔야 해요");

    // 색상 우선순위: 적색 > 흑색 > 흰색 > 녹색 > 황금색 > 갈색
    final Map<ToiletColor, Integer> COLOR_PRIORITY =
        Map.of(
            ToiletColor.RED, 1,
            ToiletColor.BLACK, 2,
            ToiletColor.WHITE, 3,
            ToiletColor.GREEN, 4,
            ToiletColor.GOLD, 5,
            ToiletColor.DARK_BROWN, 6);

    List<ToiletColorCount> colorCounts = report.getMostFrequentToiletColors();

    // 색상 기록이 없는 경우 - 월별 2건 이상의 주간 리포트, 주별 2건 이상의 일간 리포트가 필요하므로 발생하지 않아야 함
    if (colorCounts.isEmpty()) {
      log.error("Monthly toilet color report mapping failed - no color records found");
      throw new ServerException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    // 색상 우선순위에 따라 정렬 (빈도수가 같으면 우선순위 높은 색상이 먼저)
    List<ToiletColorCount> sortedColorCounts =
        colorCounts.stream()
            .sorted(
                (a, b) -> {
                  // 빈도수가 다르면 빈도수로 내림차순 정렬
                  if (a.count() != b.count()) {
                    return Integer.compare(b.count(), a.count());
                  }
                  // 빈도수가 같으면 우선순위로 오름차순 정렬
                  return Integer.compare(
                      COLOR_PRIORITY.getOrDefault(a.color(), 999),
                      COLOR_PRIORITY.getOrDefault(b.color(), 999));
                })
            .toList();

    // 가장 많이 등장한 색상 (우선순위가 적용된 첫 번째 색상)
    ToiletColorCount mostFrequentColor = sortedColorCounts.getFirst();
    String titleMessage = "가장 많이 확인한 색상은\n" + mostFrequentColor.color().getValue() + "이에요";

    // 색상에 따른 경고 메시지
    String colorWarningMessage = COLOR_MESSAGE_MAP.getOrDefault(mostFrequentColor.color(), "");

    // item 매핑
    List<ColorCount> monthlyColorCount = sortedColorCounts.stream().map(ColorCount::from).toList();

    return new MonthlyColorSection(titleMessage, colorWarningMessage, monthlyColorCount);
  }

  /** 배변 통증 분포 섹션 매핑 */
  public MonthlyPainSection mapPain(MonthlyReport report) {
    ToiletPainDistribution toiletPainDistribution = report.getToiletPainDistribution();

    // title message
    String titleMessage = "이번 달은 배를 부여잡은 날들이\n" + toiletPainDistribution.getPainfulDay() + "회 있었어요";

    return new MonthlyPainSection(
        titleMessage,
        toiletPainDistribution.veryLow(),
        toiletPainDistribution.low(),
        toiletPainDistribution.medium(),
        toiletPainDistribution.high(),
        toiletPainDistribution.veryHigh(),
        fromPainDiff(toiletPainDistribution.painDiff()));
  }

  ToiletPainComparison fromPainDiff(int painDiff) {
    if (painDiff > 0) {
      return new ToiletPainComparison("increased", painDiff);
    } else if (painDiff == 0) {
      return new ToiletPainComparison("same", painDiff);
    } else {
      return new ToiletPainComparison("decreased", -painDiff);
    }
  }

  /** 배변 시간대 분포 섹션 매핑 */
  public MonthlyPeriodSection mapPeriodSection(MonthlyReport report) {
    final String titleMessage = "이번 달은 주로\n%s에 성공했어요";

    List<ToiletPeriodCount> periodCounts = report.getToiletPeriodCounts();
    ToiletPeriodCount mostFrequent = periodCounts.getFirst();

    // title message
    StringBuilder builder = new StringBuilder(mostFrequent.period().getValue());
    if (periodCounts.size() >= 2 && periodCounts.get(1).count() == mostFrequent.count()) {
      builder.append(",").append(periodCounts.get(1).period().getValue());
    }

    if (periodCounts.size() >= 3 && periodCounts.getLast().count() == mostFrequent.count()) {
      builder.append(",").append(periodCounts.getLast().period().getValue());
    }

    // items mapping
    List<MonthlyToiletPeriod> items =
        periodCounts.stream()
            .map(
                periodCount ->
                    new MonthlyToiletPeriod(periodCount.period().getValue(), periodCount.count()))
            .toList();

    return new MonthlyPeriodSection(String.format(titleMessage, builder.toString()), items);
  }

  /** 배변 소요 시간 섹션 매핑 */
  public MonthlyTimeDistributionSection mapTimeDistribution(MonthlyReport report) {
    ToiletTimeDistribution distribution = report.getToiletTimeDistribution();

    int within5min = distribution.within5min();
    int over5min = distribution.over5min();
    int over10min = distribution.over10min();

    // 최대값 찾기
    int maxCount = Math.max(within5min, Math.max(over5min, over10min));

    // 최대값과 같은 값들의 개수 세기
    int maxCountOccurrences = 0;
    if (within5min == maxCount) {
      maxCountOccurrences++;
    }
    if (over5min == maxCount) {
      maxCountOccurrences++;
    }
    if (over10min == maxCount) {
      maxCountOccurrences++;
    }

    String titleMessage =
        getTimeDistributionTitleMessage(maxCount, maxCountOccurrences, within5min, over5min);

    String warningMessage = over10min == maxCount ? "소요 시간이 10분이 넘으면 변비 · 치질 위험도가 올라가요" : null;

    return new MonthlyTimeDistributionSection(
        titleMessage, within5min, over5min, over10min, warningMessage);
  }

  private String getTimeDistributionTitleMessage(
      int maxCount, int maxCountOccurrences, int within5min, int over5min) {
    if (maxCountOccurrences == 3) {
      return "이번 달은 화장실에서\n보낸 시간이 매번 달랐어요";
    }
    if (maxCountOccurrences == 2) {
      return "이번 달은 화장실에서\n짧고 긴 시간 모두를 경험했어요";
    }
    if (maxCount == within5min) {
      return "배변 소요 시간은\n주로 5분 이내였어요";
    }
    if (maxCount == over5min) {
      return "배변 소요 시간은\n주로 5분 이상이었어요";
    }
    return "배변 소요 시간은\n주로 10분 이상이었어요";
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

    // 내부 맵을 안전하게 노출하는 조회 함수 (NONE/NULL 폴백 포함)
    static HeroCharacter lookupHero(ToiletEvaluationLevel level) {
      ToiletEvaluationLevel safeLevel =
          (level == null || level == ToiletEvaluationLevel.NONE)
              ? ToiletEvaluationLevel.AVERAGE
              : level;
      return characterMap.getOrDefault(safeLevel, characterMap.get(ToiletEvaluationLevel.AVERAGE));
    }

    private static final String message =
        "전문가의 상담이 필요해요. 복통이 매우 심했다면 단순한 식사 문제를 넘어서 장염이나 자극적인 음식으로 인한 장 트러블일 수 있습니다.";

    static DailyToiletReportDto map(
        depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport dailyToiletReport) {
      if (dailyToiletReport.getLevel() == ToiletEvaluationLevel.NONE) {
        return null;
      }
      HeroCharacter heroCharacter = characterMap.get(dailyToiletReport.getLevel());
      return new DailyToiletReportDto(
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
}
