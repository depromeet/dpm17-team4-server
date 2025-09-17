package depromeet.lessonfour.server.foods.schemas.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FoodDto {
  private UUID id;
  private String name;

  @JsonProperty("score")
  private double foodScore;
}
