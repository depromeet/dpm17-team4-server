package depromeet.lessonfour.server.common.exception.handler;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalExceptionHandlerTest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  @DisplayName("존재하지 않는 경로 호출 시 404 Not Found 반환")
  void whenNonExistingPathCalled_thenReturnNotFoundException() {
    given()
        .when()
        .get("/non-existent-path") // 존재하지 않는 엔드포인트
        .then()
        .statusCode(404); // 404 기대
  }
}
