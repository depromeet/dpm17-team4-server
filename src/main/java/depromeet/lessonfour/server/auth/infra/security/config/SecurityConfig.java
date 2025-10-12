package depromeet.lessonfour.server.auth.infra.security.config;

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

import depromeet.lessonfour.server.auth.infra.security.jwt.JwtAuthenticationFilter;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtAuthenticationProvider;
import depromeet.lessonfour.server.auth.infra.security.jwt.entrypoint.JwtAuthenticationEntryPoint;
import depromeet.lessonfour.server.auth.infra.security.jwt.handler.JwtAccessDeniedHandler;
import depromeet.lessonfour.server.auth.infra.security.kakao.OAuthorizationRequestResolver;
import depromeet.lessonfour.server.auth.infra.security.rest.RestAuthenticationFilter;
import depromeet.lessonfour.server.auth.infra.security.rest.RestAuthenticationProvider;
import depromeet.lessonfour.server.auth.infra.security.rest.handler.RestAuthenticationFailureHandler;
import depromeet.lessonfour.server.auth.infra.security.rest.handler.RestAuthenticationSuccessHandler;
import depromeet.lessonfour.server.common.api.config.CorsConfig;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final ObjectMapper objectMapper;
  private final CorsConfig corsConfig;

  /** 문서 조회 API에 대한 필터 체인 */
  @Bean
  @Order(0)
  public SecurityFilterChain docsFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "")
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
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
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
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

  @Bean
  @Order(2)
  public SecurityFilterChain oAuth2LoginFilterChain(
      HttpSecurity http, OAuthorizationRequestResolver oAuthorizationRequestResolver)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/", "/api/v1/auth/kakao/**", "/api/v1/auth/apple/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauth2 ->
                oauth2.authorizationEndpoint(
                    endpoint ->
                        endpoint
                            .baseUri("/oauth2/authorization")
                            .authorizationRequestResolver(oAuthorizationRequestResolver)));

    return http.build();
  }

  /** 일반 API 요청에 대한 필터 체인 */
  @Bean
  @Order(3)
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
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
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
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler));

    return http.build();
  }
}
