package depromeet.lessonfour.server.food.app.dto.request;

public record FoodSearchRequestDto(String query, Integer count) {
  public FoodSearchRequestDto {
    // 기본값 설정
    if (query == null) {
      query = "";
    }
    if (count == null) {
      count = 10;
    }

    // Validation 수행
    validateQuery(query);
    validateCount(count);
  }

  private void validateQuery(String query) {
    if (query == null || query.trim().isEmpty()) {
      throw new IllegalArgumentException("query는 필수이며 빈 문자열일 수 없습니다");
    }
  }

  private void validateCount(Integer count) {
    if (count == null) {
      throw new IllegalArgumentException("count는 필수입니다");
    }
    if (count < 0) {
      throw new IllegalArgumentException("count는 0 이상이어야 합니다");
    }
    if (count > 100) {
      throw new IllegalArgumentException("count는 100 이하여야 합니다");
    }
  }
}
