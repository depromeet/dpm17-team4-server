package depromeet.lessonfour.server.common.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 모든 Origin 허용 (앞단의 로드밸런서/API Gateway에서 CORS 처리)
    configuration.addAllowedOriginPattern("*");

    // 허용할 HTTP 메서드 설정
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // 허용할 헤더 설정
    configuration.setAllowedHeaders(Arrays.asList("*"));

    // 인증 정보 포함 허용
    configuration.setAllowCredentials(true);

    // Preflight 요청 캐시 시간 (초)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
