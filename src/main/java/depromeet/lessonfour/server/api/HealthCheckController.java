package depromeet.lessonfour.server.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "서버 상태 확인 API")
public class HealthCheckController {

  @GetMapping
  @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 동작하는지 확인합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "서버 정상 동작",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(example = "{\"status\": \"UP\"}")))
  public ResponseEntity<Map<String, Object>> healthCheck() {
    return ResponseEntity.ok(Map.of("status", "UP"));
  }
}
