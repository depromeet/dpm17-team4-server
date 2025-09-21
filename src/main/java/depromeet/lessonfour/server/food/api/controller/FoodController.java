package depromeet.lessonfour.server.food.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.api.dto.ErrorResponse;
import depromeet.lessonfour.server.food.app.dto.request.FoodSearchRequestDto;
import depromeet.lessonfour.server.food.app.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/foods")
@Validated
@RestController
public class FoodController {

  private final FoodService foodService;

  @Operation(summary = "음식 검색 API")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
    @ApiResponse(responseCode = "400", description = "invalid request"),
  })
  @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> search(
      @RequestParam String query, @RequestParam(defaultValue = "10") int count) {
    try {
      FoodSearchRequestDto request = new FoodSearchRequestDto(query, count);
      return ResponseEntity.ok(foodService.search(request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(ErrorResponse.of(ErrorCode.INVALID_FIELD_ERROR, e.getMessage()));
    }
  }
}
