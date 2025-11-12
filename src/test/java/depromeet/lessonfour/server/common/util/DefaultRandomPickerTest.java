package depromeet.lessonfour.server.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultRandomPickerTest {

  private final DefaultRandomPicker randomPicker = new DefaultRandomPicker();

  @Test
  @DisplayName("[Given] 5개 이상의 아이템 [When] pickRandomly 호출 (count 없음) [Then] 정확히 3개 반환")
  void givenMoreThanThreeItems_whenPickRandomlyWithoutCount_thenReturnsExactlyThreeItems() {
    // Given
    List<String> items = Arrays.asList("ITEM1", "ITEM2", "ITEM3", "ITEM4", "ITEM5");

    // When
    List<String> result = randomPicker.pickRandomly(items);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result).isSubsetOf(items);
  }

  @Test
  @DisplayName("[Given] 정확히 3개의 아이템 [When] pickRandomly 호출 (count 없음) [Then] 3개 모두 반환")
  void givenExactlyThreeItems_whenPickRandomlyWithoutCount_thenReturnsAllThreeItems() {
    // Given
    List<String> items = Arrays.asList("ITEM1", "ITEM2", "ITEM3");

    // When
    List<String> result = randomPicker.pickRandomly(items);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result).containsExactlyInAnyOrderElementsOf(items);
  }

  @Test
  @DisplayName("[Given] 3개 미만의 아이템 [When] pickRandomly 호출 (count 없음) [Then] 전체 아이템 반환")
  void givenLessThanThreeItems_whenPickRandomlyWithoutCount_thenReturnsAllItems() {
    // Given
    List<String> items = Arrays.asList("ITEM1", "ITEM2");

    // When
    List<String> result = randomPicker.pickRandomly(items);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrderElementsOf(items);
  }

  @Test
  @DisplayName("[Given] 빈 리스트 [When] pickRandomly 호출 (count 없음) [Then] 빈 리스트 반환")
  void givenEmptyList_whenPickRandomlyWithoutCount_thenReturnsEmptyList() {
    // Given
    List<String> items = Arrays.asList();

    // When
    List<String> result = randomPicker.pickRandomly(items);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("[Given] null [When] pickRandomly 호출 (count 없음) [Then] 빈 리스트 반환")
  void givenNull_whenPickRandomlyWithoutCount_thenReturnsEmptyList() {
    // Given
    List<String> items = null;

    // When
    List<String> result = randomPicker.pickRandomly(items);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("[Given] 아이템 리스트 [When] pickRandomly 호출 with count=5 [Then] 요청한 개수 반환")
  void givenItems_whenPickRandomlyWithSpecificCount_thenReturnsRequestedCount() {
    // Given
    List<String> items = Arrays.asList("ITEM1", "ITEM2", "ITEM3", "ITEM4", "ITEM5", "ITEM6");
    int count = 5;

    // When
    List<String> result = randomPicker.pickRandomly(items, count);

    // Then
    assertThat(result).hasSize(5);
    assertThat(result).isSubsetOf(items);
  }
}
