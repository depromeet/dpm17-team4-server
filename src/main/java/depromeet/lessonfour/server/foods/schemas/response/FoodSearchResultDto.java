package depromeet.lessonfour.server.foods.schemas.response;

import java.util.List;

public record FoodSearchResultDto(List<FoodSearchItem> items) {}
