package depromeet.lessonfour.server.foods.schemas.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FoodSearchRequestDto(
    @NotBlank String query,
    @NotNull @Min(0) Integer count) {}
