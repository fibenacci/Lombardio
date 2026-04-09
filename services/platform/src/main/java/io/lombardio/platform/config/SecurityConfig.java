/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.config;

import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import io.lombardio.platform.auth.infrastructure.security.OperatorSessionAuthenticationFilter;
import io.lombardio.platform.security.KeycloakJwtAuthenticationConverter;
import io.lombardio.platform.security.KeycloakJwtValidators;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({
  AppCorsProperties.class,
  OperatorSessionProperties.class,
  OperatorBffProperties.class
})
public class SecurityConfig {

  @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
  private String jwkSetUri;

  @Value("${app.security.operator-client-id:${KEYCLOAK_OPERATOR_CLIENT_ID:lombardio-app}}")
  private String operatorClientId;

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      StoredOperatorSessionService storedOperatorSessionService,
      OperatorSessionProperties operatorSessionProperties)
      throws Exception {
    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(
                        request -> request.getHeader("Authorization") != null,
                        new AntPathRequestMatcher("/api/v1/platform/health"),
                        new AntPathRequestMatcher("/api/v1/platform/auth/login"),
                        new AntPathRequestMatcher("/actuator/**"))
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/platform/health")
                    .permitAll()
                    .requestMatchers("/api/v1/platform/auth/login")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/platform/auth/refresh",
                        "/api/v1/platform/auth/logout",
                        "/api/v1/platform/auth/me")
                    .authenticated()
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));

    http.addFilterBefore(
        new OperatorSessionAuthenticationFilter(
            storedOperatorSessionService, operatorSessionProperties),
        BearerTokenAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    jwtDecoder.setJwtValidator(
        KeycloakJwtValidators.operatorAccessTokenValidator(operatorClientId));
    return jwtDecoder;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(AppCorsProperties corsProperties) {
    if (corsProperties == null) {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
      return source;
    }

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProperties.allowedOrigins());
    configuration.setAllowedMethods(corsProperties.allowedMethods());
    configuration.setAllowedHeaders(corsProperties.allowedHeaders());
    configuration.setExposedHeaders(corsProperties.exposedHeaders());
    configuration.setMaxAge(corsProperties.maxAgeSeconds());
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
