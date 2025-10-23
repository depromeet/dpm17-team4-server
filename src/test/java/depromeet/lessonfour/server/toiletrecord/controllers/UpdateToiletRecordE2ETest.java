package depromeet.lessonfour.server.toiletrecord.controllers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
	scripts = "/sql/cleanup.sql",
	config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
	executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class UpdateToiletRecordE2ETest {

	@LocalServerPort int port;

	@Autowired JwtTokenGenerator jwtTokenGenerator;
	@Autowired UserRepository userRepository;
	@Autowired PasswordEncoder passwordEncoder;

	private String token; // "Bearer xxx"
	private User me;

	private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

		me = userRepository.save(User.register(
			"poo-update@example.com",
			"update-user",
			passwordEncoder.encode("pw1234")
		));
		token = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(me));
	}

	private int createToiletRecord(LocalDateTime occurredAt, boolean isSuccessful, int pain, int duration) {
		// LinkedHashMap을 써서 직렬화 시 필드 순서를 고정(가독성용)
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("occurredAt", occurredAt.format(DT));
		body.put("isSuccessful", isSuccessful);
		// color/shape 생략 → null 허용 검증
		body.put("pain", pain);
		body.put("duration", duration);
		// note 생략

		return given()
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(body)
			.when()
			.post("/api/v1/poo-records")
			.then()
			.statusCode(HttpStatus.OK.value())
			// 응답 바디의 status(비즈니스 코드)는 201
			.body("status", equalTo(201))
			.body("data.id", notNullValue())
			.body("data.userId", equalTo(me.getId().intValue()))
			.body("data.occurredAt", notNullValue())
			.body("data.isSuccessful", equalTo(isSuccessful))
			.body("data.color", nullValue())
			.body("data.shape", nullValue())
			.body("data.pain", equalTo(pain))
			.body("data.duration", equalTo(duration))
			.extract()
			.path("data.id");
	}

	@Test
	@DisplayName("생성: color/shape를 생략해도(=null) 성공한다")
	void create_withoutColorShape_success() {
		createToiletRecord(LocalDateTime.of(2024, 7, 1, 8, 30), true, 10, 5);
	}

	@Test
	@DisplayName("부분수정: note만 수정(다른 필드 미포함) 가능")
	void patch_partial_update_note_only() {
		int id = createToiletRecord(LocalDateTime.of(2024, 7, 2, 9, 0), true, 5, 7);

		var patchBody = Map.of(
			"note", "after breakfast"
		);

		given()
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(patchBody)
			.when()
			.patch("/api/v1/poo-records/{id}", id)
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("status", equalTo(200))
			.body("data.id", equalTo(id))
			.body("data.userId", equalTo(me.getId().intValue()))
			.body("data.note", equalTo("after breakfast"));
	}

	@Test
	@DisplayName("부분수정: 나중에 color/shape 채워 넣어도 된다")
	void patch_fill_color_shape_later() {
		int id = createToiletRecord(LocalDateTime.of(2024, 7, 3, 10, 0), true, 20, 10);

		var patchBody = Map.of(
			"color", "DEFAULT",
			"shape", "BANANA"
		);

		given()
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(patchBody)
			.when()
			.patch("/api/v1/poo-records/{id}", id)
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("status", equalTo(200))
			.body("data.id", equalTo(id))
			.body("data.userId", equalTo(me.getId().intValue()))
			.body("data.color", equalTo("DEFAULT"))
			.body("data.shape", equalTo("BANANA"));
	}

	@Test
	@DisplayName("유효하지 않은 enum 값이면 400을 반환한다")
	void patch_invalid_enum_returns_400() {
		int id = createToiletRecord(LocalDateTime.of(2024, 7, 4, 11, 0), false, 0, 5);

		var patchBody = Map.of(
			"color", "NOT_A_COLOR"
		);

		given()
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(patchBody)
			.when()
			.patch("/api/v1/poo-records/{id}", id)
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value());
	}
}