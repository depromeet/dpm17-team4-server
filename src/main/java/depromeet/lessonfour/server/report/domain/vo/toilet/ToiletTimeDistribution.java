package depromeet.lessonfour.server.report.domain.vo.toilet;

import java.util.List;

public record ToiletTimeDistribution(int within5min, int over5min, int over10min) {

  /** 작은 시간부터 순서대로 리스트로 반환 */
  public List<Integer> toList() {
    return List.of(within5min, over5min, over10min);
  }
}
