package depromeet.lessonfour.server.foods.schemas.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchResultDto {
  private List<FoodDto> items;
}
