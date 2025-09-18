package depromeet.lessonfour.server.foods.schemas.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FoodSearchItem {
  private UUID id;
  private String name;
}
