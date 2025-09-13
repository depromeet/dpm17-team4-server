package depromeet.lessonfour.server.foods.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.foods.schemas.response.FoodSearchResultDto;
import depromeet.lessonfour.server.foods.services.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/foods")
public class FoodController {

  private final FoodService foodService;

  @Operation(summary = "음식 검색 API")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
  })
  @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FoodSearchResultDto> search(@RequestParam String query) {
    return ResponseEntity.ok(foodService.search(query));
  }
}
