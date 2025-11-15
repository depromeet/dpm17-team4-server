package depromeet.lessonfour.server.recordquery.api.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.recordquery.api.dto.HomeOverviewResponse;
import depromeet.lessonfour.server.recordquery.app.dto.DailyOverviewDto;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluationLevel;

@Component
public class HomeMapper {

  private static final Map<ToiletEvaluationLevel, ToiletHeroAssets> HOME_ASSETS =
      Map.of(
          ToiletEvaluationLevel.VERY_BAD,
          new ToiletHeroAssets(
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/very_bad.png",
              List.of("#A4141E", "#FF535F")),
          ToiletEvaluationLevel.BAD,
          new ToiletHeroAssets(
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/bad.png",
              List.of("#DD5612", "#F6A85F")),
          ToiletEvaluationLevel.AVERAGE,
          new ToiletHeroAssets(
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/normal.png",
              List.of("#2B42B4", "#8F58FF")),
          ToiletEvaluationLevel.GOOD,
          new ToiletHeroAssets(
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/good.png",
              List.of("#134DB1", "#588DFF")),
          ToiletEvaluationLevel.VERY_GOOD,
          new ToiletHeroAssets(
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/very_good.png",
              List.of("#0C7C30", "#7DD357")));

  record ToiletHeroAssets(String image, List<String> backgroundColors) {}

  public HomeOverviewResponse map(DailyOverviewDto dto) {

    ToiletEvaluationLevel toiletEvaluationLevel = dto.toiletEvaluationLevel();

    // fallback 처리
    if (toiletEvaluationLevel == null || toiletEvaluationLevel == ToiletEvaluationLevel.NONE) {
      toiletEvaluationLevel = ToiletEvaluationLevel.AVERAGE;
    }

    ToiletHeroAssets asset = HOME_ASSETS.get(toiletEvaluationLevel);

    return new HomeOverviewResponse(
        dto.toiletRecordCount(), dto.hasActivityRecord(), asset.image, asset.backgroundColors);
  }
}
