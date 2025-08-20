package depromeet.lessonfour.server.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HelloControllerE2ETest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void givenHelloController_whenHelloCalled_thenShouldReturnHelloWorldMessage() {
        given()
            .when()
                .get("/api/hello")
            .then()
                .statusCode(200)
                .contentType("text/plain;charset=UTF-8")
                .body(equalTo("Hello, World!"));
    }
}
