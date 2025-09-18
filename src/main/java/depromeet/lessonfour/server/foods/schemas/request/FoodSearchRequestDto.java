package depromeet.lessonfour.server.foods.schemas.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FoodSearchRequestDto {
  private String query;
  private Integer count = 10;
}
