package depromeet.lessonfour.server.auth.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import depromeet.lessonfour.server.auth.security.jwt.JwtAuthenticationFilter;
import depromeet.lessonfour.server.auth.security.jwt.JwtAuthenticationProvider;
import depromeet.lessonfour.server.auth.security.jwt.entrypoint.JwtAuthenticationEntryPoint;
import depromeet.lessonfour.server.auth.security.jwt.handler.JwtAccessDeniedHandler;
import depromeet.lessonfour.server.auth.security.rest.RestAuthenticationFilter;
import depromeet.lessonfour.server.auth.security.rest.RestAuthenticationProvider;
import depromeet.lessonfour.server.auth.security.rest.handler.RestAuthenticationFailureHandler;
import depromeet.lessonfour.server.auth.security.rest.handler.RestAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final ObjectMapper objectMapper;

  /** 문서 조회 API에 대한 필터 체인 */
  @Bean
  @Order(0)
  public SecurityFilterChain docsFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }

  /** 로컬 로그인 API 요청에 대한 필터 체인 */
  @Bean
  @Order(1)
  public SecurityFilterChain loginFilterChain(
      HttpSecurity http,
      RestAuthenticationProvider restAuthenticationProvider,
      RestAuthenticationSuccessHandler restAuthenticationSuccessHandler,
      RestAuthenticationFailureHandler restAuthenticationFailureHandler)
      throws Exception {

    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(restAuthenticationProvider);
    AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

    return http.securityMatcher("/api/v1/auth/login")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(AbstractHttpConfigurer::disable)
        .anonymous(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .addFilterBefore(
            new RestAuthenticationFilter(
                authenticationManager,
                restAuthenticationSuccessHandler,
                restAuthenticationFailureHandler,
                objectMapper),
            UsernamePasswordAuthenticationFilter.class)
        .authenticationManager(authenticationManager)
        .build();
  }

  /** 일반 API 요청에 대한 필터 체인 */
  @Bean
  @Order(2)
  public SecurityFilterChain apiFilterChain(
      HttpSecurity http,
      JwtAuthenticationProvider jwtAuthenticationProvider,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtAccessDeniedHandler jwtAccessDeniedHandler)
      throws Exception {

    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
    AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

    http.securityMatcher("/api/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/v1/health",
                        "/api/v1/auth/signup",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/kakao/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(
            new JwtAuthenticationFilter(authenticationManager),
            UsernamePasswordAuthenticationFilter.class)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authenticationManager(authenticationManager)
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint(objectMapper))
                    .accessDeniedHandler(new JwtAccessDeniedHandler(objectMapper)));

    return http.build();
  }
}
