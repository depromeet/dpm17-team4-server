package depromeet.lessonfour.server.common.security;

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

import depromeet.lessonfour.server.common.security.rest.RestAuthenticationFilter;
import depromeet.lessonfour.server.common.security.rest.RestAuthenticationProvider;
import depromeet.lessonfour.server.common.security.rest.handler.RestAuthenticationFailureHandler;
import depromeet.lessonfour.server.common.security.rest.handler.RestAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final RestAuthenticationProvider restAuthenticationProvider;
  private final RestAuthenticationSuccessHandler restAuthenticationSuccessHandler;
  private final RestAuthenticationFailureHandler restAuthenticationFailureHandler;

  @Bean
  @Order(1)
  public SecurityFilterChain loginFilterChain(HttpSecurity http) throws Exception {

    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(restAuthenticationProvider);
    AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

    return http.securityMatcher("/api/auth/**", "/api/hello/**")
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
}
