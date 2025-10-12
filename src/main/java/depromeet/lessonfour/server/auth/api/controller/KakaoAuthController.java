package depromeet.lessonfour.server.auth.api.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.auth.api.dto.KakaoTokenRequestDto;
import depromeet.lessonfour.server.auth.api.util.RefreshTokenCookieGenerator;
import depromeet.lessonfour.server.auth.app.service.KakaoAuthService;
import depromeet.lessonfour.server.user.app.dto.response.AuthResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "카카오 인증", description = "카카오 인증에 대한 API 문서입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/kakao")
public class KakaoAuthController {

  private final KakaoAuthService kakaoAuthService;

  @Value("${frontend.url}")
  private String frontendUrl;

  @Operation(summary = "카카오 로그인", description = "카카오를 통해 로그인을 진행합니다.")
  @PostMapping("/login")
  public ResponseEntity<Void> kakaoLogin(
      @RequestParam(required = false) String redirectUri,
      @RequestParam(required = false) String responseType) {
    String redirectUrl =
        "/oauth2/authorization/kakao?redirectUri=" + redirectUri + "&responseType=" + responseType;
    return ResponseEntity.status(302).header("Location", redirectUrl).build();
  }

  @Operation(summary = "카카오 인증 토큰 발급", description = "카카오 인증 코드를 받아서 토큰을 JSON 형태로 반환합니다.")
  @PostMapping("/token")
  public ResponseEntity<AuthResponseDto> getKakaoToken(@RequestBody KakaoTokenRequestDto request) {
    try {
      // JSON 응답에서는 access token도 포함
      AuthResponseDto authResult = kakaoAuthService.login(request.code(), true);

      return ResponseEntity.ok()
          .cacheControl(CacheControl.noStore().mustRevalidate())
          .body(authResult);
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Authentication failed: " + e.getMessage());
    }
  }

  @Hidden
  @GetMapping("/callback")
  public ResponseEntity<Void> kakaoLegacyCallback(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String error,
      @RequestParam(required = false) String state) {

    // state에서 redirectUri와 responseType 파싱
    String clientRedirectUri = frontendUrl;
    String requestResponseType = null;

    if (state != null && !state.isBlank()) {
      String[] stateParts = state.split("\\|responseType=", 2); // 최대 2개로 분할

      // redirectUri 파싱 (첫 번째 부분)
      String parsedRedirectUri = stateParts[0];
      if (parsedRedirectUri != null && !parsedRedirectUri.isBlank()) {
        clientRedirectUri = parsedRedirectUri;
      }
      // parsedRedirectUri가 빈 문자열이면 기본값(frontendUrl) 유지

      // responseType 파싱 (두 번째 부분)
      if (stateParts.length > 1 && stateParts[1] != null && !stateParts[1].isBlank()) {
        requestResponseType = stateParts[1];
      }
    }

    if (error != null) {
      String errorUrl =
          UriComponentsBuilder.fromUriString(clientRedirectUri)
              .queryParam("error", "OAuth error: " + error)
              .encode(StandardCharsets.UTF_8)
              .build()
              .toUriString();
      return ResponseEntity.status(302).header("Location", errorUrl).build();
    }

    if (code != null) {
      try {
        // responseType이 "code"인 경우에만 auth code를 그대로 전달, 그 외에는 토큰 직접 발급
        if ("code".equals(requestResponseType)) {
          // auth code를 그대로 프론트엔드로 전달
          String successUrl =
              UriComponentsBuilder.fromUriString(clientRedirectUri)
                  .queryParam("code", code)
                  .encode(StandardCharsets.UTF_8)
                  .build()
                  .toUriString();

          return ResponseEntity.status(302)
              .header("Location", successUrl)
              .cacheControl(CacheControl.noStore().mustRevalidate())
              .build();
        } else {
          // 기본 방식: 토큰 직접 발급 (responseType이 없거나 "code"가 아닌 경우)
          AuthResponseDto authResult = kakaoAuthService.login(code);

          // redirectUrl의 호스트를 도메인으로 사용
          String domain = null;
          try {
            URI redirectUri = URI.create(clientRedirectUri);
            domain = redirectUri.getHost();
          } catch (Exception e) {
            // URI 파싱 실패 시 도메인 없이 진행
          }

          String successUrl =
              UriComponentsBuilder.fromUriString(clientRedirectUri)
                  .queryParam("id", authResult.id())
                  .queryParam("nickname", authResult.nickname())
                  .queryParam("profileImage", authResult.profileImage())
                  .queryParam("isNew", authResult.isNew())
                  .queryParam("providerType", authResult.provider().getType())
                  .encode(StandardCharsets.UTF_8)
                  .build()
                  .toUriString();

          ResponseEntity.BodyBuilder responseBuilder =
              ResponseEntity.status(302)
                  .header("Location", successUrl)
                  .cacheControl(CacheControl.noStore().mustRevalidate());

          // 도메인을 성공적으로 가져올 수 있으면 2개 쿠키 생성
          if (domain != null && !domain.isBlank()) {
            ResponseCookie refreshTokenCookieWithDomain =
                RefreshTokenCookieGenerator.generate(authResult.refreshToken(), domain);
            ResponseCookie refreshTokenCookieWithoutDomain =
                RefreshTokenCookieGenerator.generate(authResult.refreshToken());

            return responseBuilder
                .header("Set-Cookie", refreshTokenCookieWithDomain.toString())
                .header("Set-Cookie", refreshTokenCookieWithoutDomain.toString())
                .build();
          } else {
            // 도메인을 가져올 수 없으면 1개 쿠키만 생성
            ResponseCookie refreshTokenCookie =
                RefreshTokenCookieGenerator.generate(authResult.refreshToken());

            return responseBuilder.header("Set-Cookie", refreshTokenCookie.toString()).build();
          }
        }

      } catch (Exception e) {
        String errorUrl =
            UriComponentsBuilder.fromUriString(clientRedirectUri)
                .queryParam("error", "Authentication failed: " + e.getMessage())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
        return ResponseEntity.status(302).header("Location", errorUrl).build();
      }
    }

    String errorUrl =
        UriComponentsBuilder.fromUriString(clientRedirectUri)
            .queryParam("error", "No code or error received")
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString();
    return ResponseEntity.status(302).header("Location", errorUrl).build();
  }
}
