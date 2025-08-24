package depromeet.lessonfour.server.auth.config;

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

import depromeet.lessonfour.server.auth.config.jwt.JwtAuthenticationFilter;
import depromeet.lessonfour.server.auth.config.jwt.JwtAuthenticationProvider;
import depromeet.lessonfour.server.auth.config.jwt.entrypoint.JwtAuthenticationEntryPoint;
import depromeet.lessonfour.server.auth.config.jwt.handler.JwtAccessDeniedHandler;
import depromeet.lessonfour.server.auth.config.rest.RestAuthenticationFilter;
import depromeet.lessonfour.server.auth.config.rest.RestAuthenticationProvider;
import depromeet.lessonfour.server.auth.config.rest.handler.RestAuthenticationFailureHandler;
import depromeet.lessonfour.server.auth.config.rest.handler.RestAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

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

    return http.securityMatcher("/api/auth/login", "/api/hello/**") // 로그인 및 테스트용 API 처리
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
                restAuthenticationFailureHandler),
            UsernamePasswordAuthenticationFilter.class)
        .authenticationManager(authenticationManager)
        .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain apiFilterChain(
      HttpSecurity http, JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {

    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
    AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

    http.securityMatcher("/api/**") // 그 외 API 요청은 전부 여기서 JWT 검증
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/auth/register")
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
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                    .accessDeniedHandler(new JwtAccessDeniedHandler()));

    return http.build();
  }
}
