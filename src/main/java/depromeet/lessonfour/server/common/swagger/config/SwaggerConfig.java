package depromeet.lessonfour.server.common.swagger.config;

import java.util.Collections;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import depromeet.lessonfour.server.common.swagger.annotation.DisableSwaggerSecurity;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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

    // HTTP와 HTTPS 서버 모두 추가 (SSL 문제 해결을 위해)
    return new OpenAPI()
        .addServersItem(new Server().url(serverUrl).description("Production Server (HTTPS)"))
        .addServersItem(
            new Server()
                .url(serverUrl.replace("https://", "http://"))
                .description("HTTP Server (No SSL)"))
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

      boolean isSecured = operation.getSecurity() != null && !operation.getSecurity().isEmpty();

      // 공통 응답 스키마 적용
      addCommonResponses(operation, isSecured);

      return operation;
    };
  }

  private void addCommonResponses(Operation operation, boolean isSecured) {
    if (operation.getResponses() == null) {
      operation.setResponses(new ApiResponses());
    }

    addResponseIfMissing(operation, HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", false, isSecured);
    addResponseIfMissing(operation, HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", true, isSecured);
    addResponseIfMissing(operation, HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", true, isSecured);
    addResponseIfMissing(
        operation, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", false, isSecured);
  }

  private void addResponseIfMissing(
      Operation operation,
      HttpStatus status,
      String message,
      boolean securedOnly,
      boolean isSecured) {

    String code = String.valueOf(status.value());

    if ((!securedOnly || isSecured) && !operation.getResponses().containsKey(code)) {
      operation.getResponses().addApiResponse(code, createErrorResponse(status, message));
    }
  }

  private ApiResponse createErrorResponse(HttpStatus status, String message) {
    ApiResponse response = new ApiResponse();
    response.setDescription(message);

    Schema<?> errorSchema =
        new Schema<>()
            .type("object")
            .addProperty("status", new Schema<>().type("integer").example(status.value()))
            .addProperty("message", new Schema<>().type("string").example(message))
            .addProperty("timestamp", new Schema<>().type("string").format("date-time"));

    MediaType mediaType = new MediaType().schema(errorSchema);
    Content content = new Content().addMediaType("application/json", mediaType);
    response.setContent(content);

    return response;
  }

  private Info apiInfo() {
    return new Info().title("DPM team4 Project API").description("디프만 17기 4팀").version("1.0.0");
  }
}
