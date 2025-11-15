package depromeet.lessonfour.server.recordquery.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.recordquery.api.dto.HomeOverviewResponse;
import depromeet.lessonfour.server.recordquery.app.dto.DailyOverviewDto;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluationLevel;

class HomeMapperTest {

  HomeMapper mapper = new HomeMapper();

  @Test
  @DisplayName("VERY_GOOD 레벨의 히어로 에셋을 올바르게 반환한다")
  void givenVeryGoodLevel_whenHeroAssetsByLevel_thenReturnCorrectAssets() {
    // when
    HomeOverviewResponse result =
        mapper.map(new DailyOverviewDto(ToiletEvaluationLevel.VERY_GOOD, 10, true));

    // then
    assertThat(result).isNotNull();
    assertThat(result.heroImage())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/very_good.png");
    assertThat(result.heroBackgroundColors()).containsExactly("#0C7C30", "#7DD357");
  }

  @Test
  @DisplayName("GOOD 레벨의 히어로 에셋을 올바르게 반환한다")
  void givenGoodLevel_whenHeroAssetsByLevel_thenReturnCorrectAssets() {
    // when
    HomeOverviewResponse result =
        mapper.map(new DailyOverviewDto(ToiletEvaluationLevel.GOOD, 10, true));

    // then
    assertThat(result).isNotNull();
    assertThat(result.heroImage())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/good.png");
    assertThat(result.heroBackgroundColors()).containsExactly("#134DB1", "#588DFF");
  }

  @Test
  @DisplayName("AVERAGE 레벨의 히어로 에셋을 올바르게 반환한다")
  void givenAverageLevel_whenHeroAssetsByLevel_thenReturnCorrectAssets() {
    // when
    HomeOverviewResponse result =
        mapper.map(new DailyOverviewDto(ToiletEvaluationLevel.AVERAGE, 10, true));

    // then
    assertThat(result).isNotNull();
    assertThat(result.heroImage())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/normal.png");
    assertThat(result.heroBackgroundColors()).containsExactly("#2B42B4", "#8F58FF");
  }

  @Test
  @DisplayName("BAD 레벨의 히어로 에셋을 올바르게 반환한다")
  void givenBadLevel_whenHeroAssetsByLevel_thenReturnCorrectAssets() {
    // when
    HomeOverviewResponse result =
        mapper.map(new DailyOverviewDto(ToiletEvaluationLevel.BAD, 10, true));

    // then
    assertThat(result).isNotNull();
    assertThat(result.heroImage())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/bad.png");
    assertThat(result.heroBackgroundColors()).containsExactly("#DD5612", "#F6A85F");
  }

  @Test
  @DisplayName("VERY_BAD 레벨의 히어로 에셋을 올바르게 반환한다")
  void givenVeryBadLevel_whenHeroAssetsByLevel_thenReturnCorrectAssets() {
    // when
    HomeOverviewResponse result =
        mapper.map(new DailyOverviewDto(ToiletEvaluationLevel.VERY_BAD, 10, true));

    // then
    assertThat(result).isNotNull();
    assertThat(result.heroImage())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/very_bad.png");
    assertThat(result.heroBackgroundColors()).containsExactly("#A4141E", "#FF535F");
  }

  @Test
  @DisplayName("NONE 레벨은 AVERAGE 레벨의 히어로 에셋으로 폴백한다")
  void givenNoneLevel_whenHeroAssetsByLevel_thenReturnAverageLevelAssets() {
    // when
    HomeOverviewResponse result =
        mapper.map(new DailyOverviewDto(ToiletEvaluationLevel.NONE, 10, true));

    // then
    assertThat(result).isNotNull();
    assertThat(result.heroImage())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/normal.png");
    assertThat(result.heroBackgroundColors()).containsExactly("#2B42B4", "#8F58FF");
  }

  @Test
  @DisplayName("null 레벨은 AVERAGE 레벨의 히어로 에셋으로 폴백한다")
  void givenNullLevel_whenHeroAssetsByLevel_thenReturnAverageLevelAssets() {
    // when
    HomeOverviewResponse result = mapper.map(new DailyOverviewDto(null, 10, true));

    // then
    assertThat(result).isNotNull();
    assertThat(result.heroImage())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/normal.png");
    assertThat(result.heroBackgroundColors()).containsExactly("#2B42B4", "#8F58FF");
  }
}
