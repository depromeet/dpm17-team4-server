package depromeet.lessonfour.server.foods.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.foods.schemas.response.FoodSearchResultDto;
import depromeet.lessonfour.server.foods.services.FoodService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/foods")
public class FoodController {

  private final FoodService foodService;

  @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FoodSearchResultDto> search(@RequestParam String query) {
    return ResponseEntity.ok(foodService.search(query));
  }
}
