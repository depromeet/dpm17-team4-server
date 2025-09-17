package depromeet.lessonfour.server.foods.schemas.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchRequestDto {
  private String query;
  private Integer count = 10;
}
