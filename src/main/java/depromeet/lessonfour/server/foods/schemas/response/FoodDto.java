package depromeet.lessonfour.server.foods.schemas.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FoodDto {
  private UUID id;
  private String name;
  private double score;
}
