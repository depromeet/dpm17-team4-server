package depromeet.lessonfour.server.common.swagger.config;

import java.util.Collections;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import depromeet.lessonfour.server.common.swagger.annotation.DisableSwaggerSecurity;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

  @Value("${app.server.url}")
  private String serverUrl;

  @Bean
  public OpenAPI openAPI() {
    String jwt = "JWT";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

    Components components =
        new Components()
            .addSecuritySchemes(
                jwt,
                new SecurityScheme()
                    .name(jwt)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));

    return new OpenAPI()
        .addServersItem(new Server().url(serverUrl))
        .components(components)
        .info(apiInfo())
        .addSecurityItem(securityRequirement);
  }

  @Bean
  public GroupedOpenApi generalApi() {
    return GroupedOpenApi.builder()
        .group("general")
        .pathsToMatch("/api/**")
        .pathsToExclude("/api/admin/**")
        .addOperationCustomizer(customize())
        .build();
  }

  @Bean
  public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .pathsToMatch("/api/admin/**")
        .addOperationCustomizer(customize())
        .build();
  }

  @Bean
  public OperationCustomizer customize() {
    return (operation, handlerMethod) -> {
      DisableSwaggerSecurity methodAnnotation =
          handlerMethod.getMethodAnnotation(DisableSwaggerSecurity.class);
      if (methodAnnotation != null) {
        operation.setSecurity(Collections.emptyList());
      }

      // 공통 응답 스키마 적용
      addCommonResponses(operation);

      return operation;
    };
  }

  private void addCommonResponses(io.swagger.v3.oas.models.Operation operation) {
    if (operation.getResponses() == null) {
      operation.setResponses(new io.swagger.v3.oas.models.responses.ApiResponses());
    }

    // 400 Bad Request - 잘못된 요청
    if (!operation.getResponses().containsKey("400")) {
      operation.getResponses().addApiResponse("400", createErrorResponse("잘못된 요청입니다.", "400"));
    }

    // 401 Unauthorized - 인증 실패 (보안이 필요한 엔드포인트만)
    if (operation.getSecurity() != null
        && !operation.getSecurity().isEmpty()
        && !operation.getResponses().containsKey("401")) {
      operation.getResponses().addApiResponse("401", createErrorResponse("인증이 필요합니다.", "401"));
    }

    // 403 Forbidden - 권한 없음 (보안이 필요한 엔드포인트만)
    if (operation.getSecurity() != null
        && !operation.getSecurity().isEmpty()
        && !operation.getResponses().containsKey("403")) {
      operation.getResponses().addApiResponse("403", createErrorResponse("접근 권한이 없습니다.", "403"));
    }

    // 500 Internal Server Error - 서버 오류
    if (!operation.getResponses().containsKey("500")) {
      operation
          .getResponses()
          .addApiResponse("500", createErrorResponse("서버 내부 오류가 발생했습니다.", "500"));
    }
  }

  private io.swagger.v3.oas.models.responses.ApiResponse createErrorResponse(
      String description, String status) {
    io.swagger.v3.oas.models.responses.ApiResponse response =
        new io.swagger.v3.oas.models.responses.ApiResponse();
    response.setDescription(description);

    // 에러 응답 스키마 정의
    io.swagger.v3.oas.models.media.Content content = new io.swagger.v3.oas.models.media.Content();
    io.swagger.v3.oas.models.media.MediaType mediaType =
        new io.swagger.v3.oas.models.media.MediaType();

    io.swagger.v3.oas.models.media.Schema<?> errorSchema =
        new io.swagger.v3.oas.models.media.Schema<>();
    errorSchema.setType("object");
    errorSchema.addProperty(
        "status",
        new io.swagger.v3.oas.models.media.Schema<>()
            .type("integer")
            .example(Integer.parseInt(status)));
    errorSchema.addProperty(
        "message",
        new io.swagger.v3.oas.models.media.Schema<>().type("string").example(description));
    errorSchema.addProperty(
        "timestamp",
        new io.swagger.v3.oas.models.media.Schema<>().type("string").format("date-time"));

    mediaType.setSchema(errorSchema);
    content.addMediaType("application/json", mediaType);
    response.setContent(content);

    return response;
  }

  private Info apiInfo() {
    return new Info().title("DPM team4 Project API").description("디프만 17기 4팀").version("1.0.0");
  }
}
