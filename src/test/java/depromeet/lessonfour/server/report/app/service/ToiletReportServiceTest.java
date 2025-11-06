package depromeet.lessonfour.server.report.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.app.dto.response.ToiletShapeCount;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import depromeet.lessonfour.server.user.domain.entity.User;

class ToiletReportServiceTest {

  private ToiletReportService toiletReportService;

  private User testUser;

  @BeforeEach
  void setUp() {
    toiletReportService = new ToiletReportService(null, null, null);
    testUser = User.register("test@example.com", "testuser", "password123");
  }

  private ToiletRecord createMockToiletRecord(
      ToiletColor color, ToiletShape shape, LocalDateTime dateTime) {
    return ToiletRecord.register(
        testUser, true, color, shape, 10, 5, "test note", ActivityAt.from(dateTime));
  }

  @Test
  @DisplayName("getMostShape - 정상 케이스: 가장 많이 나타난 shape 3개를 내림차순으로 반환한다")
  void getMostShape_withValidRecords_returnsTop3ShapesDescending() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    // BANANA: 5개
    for (int i = 0; i < 5; i++) {
      records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    }
    // CREAM: 3개
    for (int i = 0; i < 3; i++) {
      records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.CREAM, baseTime));
    }
    // CORN: 2개
    for (int i = 0; i < 2; i++) {
      records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.CORN, baseTime));
    }
    // ROCK: 1개
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.ROCK, baseTime));

    // When
    List<ToiletShapeCount> result = toiletReportService.getMostShape(records);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).shape()).isEqualTo(ToiletShape.BANANA);
    assertThat(result.get(0).count()).isEqualTo(5);
    assertThat(result.get(1).shape()).isEqualTo(ToiletShape.CREAM);
    assertThat(result.get(1).count()).isEqualTo(3);
    assertThat(result.get(2).shape()).isEqualTo(ToiletShape.CORN);
    assertThat(result.get(2).count()).isEqualTo(2);
  }

  @Test
  @DisplayName("getMostShape - 빈 리스트가 주어지면 빈 리스트를 반환한다")
  void getMostShape_withEmptyList_returnsEmptyList() {
    // Given
    List<ToiletRecord> records = new ArrayList<>();

    // When
    List<ToiletShapeCount> result = toiletReportService.getMostShape(records);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getMostShape - null shape가 포함된 경우 필터링하여 처리한다")
  void getMostShape_withNullShape_filtersNullAndReturnsValid() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, null, baseTime)); // null shape

    // When
    List<ToiletShapeCount> result = toiletReportService.getMostShape(records);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().shape()).isEqualTo(ToiletShape.BANANA);
    assertThat(result.getFirst().count()).isEqualTo(2);
  }

  @Test
  @DisplayName("getMostShape - 모든 shape가 null인 경우 빈 리스트를 반환한다")
  void getMostShape_withAllNullShapes_returnsEmptyList() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    records.add(createMockToiletRecord(ToiletColor.DEFAULT, null, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, null, baseTime));

    // When
    List<ToiletShapeCount> result = toiletReportService.getMostShape(records);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getMostShape - shape 종류가 3개 미만인 경우 실제 개수만큼 반환한다")
  void getMostShape_withLessThan3Shapes_returnsActualCount() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.CREAM, baseTime));

    // When
    List<ToiletShapeCount> result = toiletReportService.getMostShape(records);

    // Then
    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("getMostShape - 같은 카운트를 가진 shape가 여러 개인 경우에도 3개만 반환한다")
  void getMostShape_withSameCount_returnsTop3() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    // 모든 shape가 동일하게 2개씩
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.CREAM, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.CREAM, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.CORN, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.CORN, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.ROCK, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.ROCK, baseTime));

    // When
    List<ToiletShapeCount> result = toiletReportService.getMostShape(records);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result).allMatch(count -> count.count() == 2);
  }

  @Test
  @DisplayName("getMostColor - 정상 케이스: 가장 많이 나타난 color 3개를 내림차순으로 반환한다")
  void getMostColor_withValidRecords_returnsTop3ColorsDescending() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    // DEFAULT: 5개
    for (int i = 0; i < 5; i++) {
      records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    }
    // GOLD: 3개
    for (int i = 0; i < 3; i++) {
      records.add(createMockToiletRecord(ToiletColor.GOLD, ToiletShape.BANANA, baseTime));
    }
    // DARK_BROWN: 2개
    for (int i = 0; i < 2; i++) {
      records.add(createMockToiletRecord(ToiletColor.DARK_BROWN, ToiletShape.BANANA, baseTime));
    }
    // RED: 1개
    records.add(createMockToiletRecord(ToiletColor.RED, ToiletShape.BANANA, baseTime));

    // When
    List<ToiletColorCount> result = toiletReportService.getMostColor(records);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).color()).isEqualTo(ToiletColor.DEFAULT);
    assertThat(result.get(0).count()).isEqualTo(5);
    assertThat(result.get(1).color()).isEqualTo(ToiletColor.GOLD);
    assertThat(result.get(1).count()).isEqualTo(3);
    assertThat(result.get(2).color()).isEqualTo(ToiletColor.DARK_BROWN);
    assertThat(result.get(2).count()).isEqualTo(2);
  }

  @Test
  @DisplayName("getMostColor - 빈 리스트가 주어지면 빈 리스트를 반환한다")
  void getMostColor_withEmptyList_returnsEmptyList() {
    // Given
    List<ToiletRecord> records = new ArrayList<>();

    // When
    List<ToiletColorCount> result = toiletReportService.getMostColor(records);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getMostColor - null color가 포함된 경우 필터링하여 처리한다")
  void getMostColor_withNullColor_filtersNullAndReturnsValid() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(null, ToiletShape.BANANA, baseTime)); // null color

    // When
    List<ToiletColorCount> result = toiletReportService.getMostColor(records);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().color()).isEqualTo(ToiletColor.DEFAULT);
    assertThat(result.getFirst().count()).isEqualTo(2);
  }

  @Test
  @DisplayName("getMostColor - 모든 color가 null인 경우 빈 리스트를 반환한다")
  void getMostColor_withAllNullColors_returnsEmptyList() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    records.add(createMockToiletRecord(null, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(null, ToiletShape.BANANA, baseTime));

    // When
    List<ToiletColorCount> result = toiletReportService.getMostColor(records);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getMostColor - color 종류가 3개 미만인 경우 실제 개수만큼 반환한다")
  void getMostColor_withLessThan3Colors_returnsActualCount() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.GOLD, ToiletShape.BANANA, baseTime));

    // When
    List<ToiletColorCount> result = toiletReportService.getMostColor(records);

    // Then
    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("getMostColor - 같은 카운트를 가진 color가 여러 개인 경우에도 3개만 반환한다")
  void getMostColor_withSameCount_returnsTop3() {
    // Given
    LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
    List<ToiletRecord> records = new ArrayList<>();

    // 모든 color가 동일하게 2개씩
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DEFAULT, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.GOLD, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.GOLD, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DARK_BROWN, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.DARK_BROWN, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.RED, ToiletShape.BANANA, baseTime));
    records.add(createMockToiletRecord(ToiletColor.RED, ToiletShape.BANANA, baseTime));

    // When
    List<ToiletColorCount> result = toiletReportService.getMostColor(records);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result).allMatch(count -> count.count() == 2);
  }
}
