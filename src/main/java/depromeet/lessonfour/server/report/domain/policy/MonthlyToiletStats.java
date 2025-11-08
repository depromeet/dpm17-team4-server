package depromeet.lessonfour.server.report.domain.policy;

import java.util.List;

import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.app.dto.response.ToiletShapeCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

public final class MonthlyToiletStats {
  private MonthlyToiletStats() {}

  // 통증 분포 가중평균 (버킷 대표치: 10/30/50/70/90)
  public static double avgPain(ToiletPainDistribution pd) {
    if (pd == null) return 0;
    int n = pd.veryLow() + pd.low() + pd.medium() + pd.high() + pd.veryHigh();
    if (n == 0) return 0;
    double sum =
        10.0 * pd.veryLow()
            + 30.0 * pd.low()
            + 50.0 * pd.medium()
            + 70.0 * pd.high()
            + 90.0 * pd.veryHigh();
    return sum / n;
  }

  // 소요시간 분포 평균 (대표치: 3/7.5/12분)
  public static double avgMinutes(ToiletTimeDistribution td) {
    if (td == null) return 0;
    int n = td.within5min() + td.over5min() + td.over10min();
    if (n == 0) return 0;
    double sum = 3.0 * td.within5min() + 7.5 * td.over5min() + 12.0 * td.over10min();
    return sum / n;
  }

  public static int totalRecords(ToiletTimeDistribution td) {
    if (td == null) return 0;
    return td.within5min() + td.over5min() + td.over10min();
  }

  public static boolean hasBlood(List<ToiletColorCount> colors) {
    if (colors == null) return false;
    return colors.stream().anyMatch(c -> c.color() == ToiletColor.RED && c.count() > 0);
  }

  public static boolean hasAbnormalColor(List<ToiletColorCount> colors) {
    if (colors == null) return false;
    return colors.stream()
        .anyMatch(
            c ->
                (c.color() == ToiletColor.GRAY || c.color() == ToiletColor.BLACK) && c.count() > 0);
  }

  public static boolean goodColorMostly(List<ToiletColorCount> colors) {
    if (colors == null || colors.isEmpty()) return false;
    int good =
        colors.stream()
            .filter(
                c ->
                    c.color() == ToiletColor.DARK_BROWN
                        || c.color() == ToiletColor.GOLD
                        || c.color() == ToiletColor.DEFAULT)
            .mapToInt(ToiletColorCount::count)
            .sum();
    int total = colors.stream().mapToInt(ToiletColorCount::count).sum();
    return total > 0 && good >= 0.6 * total; // 정책치 60%
  }

  public static boolean goodShapeMostly(List<ToiletShapeCount> shapes) {
    if (shapes == null || shapes.isEmpty()) return false;
    int good =
        shapes.stream()
            .filter(s -> s.shape() == ToiletShape.BANANA)
            .mapToInt(ToiletShapeCount::count)
            .sum();
    int total = shapes.stream().mapToInt(ToiletShapeCount::count).sum();
    return total > 0 && good >= 0.6 * total; // 정책치 60%
  }
}
