package depromeet.lessonfour.server.foods.schemas.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FoodSearchResultDto {
  private List<FoodDto> items;
}
