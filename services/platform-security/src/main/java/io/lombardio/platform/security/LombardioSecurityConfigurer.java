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
package io.lombardio.platform.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configurer for common security settings across Lombardio services.
 */
public class LombardioSecurityConfigurer {

  private final LombardioSecurityProperties properties;
  private final CorsConfigurationSource corsConfigurationSource;

  public LombardioSecurityConfigurer(
      LombardioSecurityProperties properties, CorsConfigurationSource corsConfigurationSource) {
    this.properties = properties;
    this.corsConfigurationSource = corsConfigurationSource;
  }

  /**
   * Applies common stateless security configuration (JWT, CORS, Stateless Sessions).
   */
  public HttpSecurity configureStatelessDefaults(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .bearerTokenResolver(
                        new CookieOrHeaderBearerTokenResolver(properties.operatorAccessCookieName()))
                    .jwt(
                        jwt ->
                            jwt.jwtAuthenticationConverter(
                                new KeycloakJwtAuthenticationConverter())));
  }

  /**
   * Exposes OpenAPI documentation endpoints without authentication.
   */
  public void configureOpenApiExemptions(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers(
                    AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                    AntPathRequestMatcher.antMatcher("/v3/api-docs.yaml"),
                    AntPathRequestMatcher.antMatcher("/v3/api-docs"),
                    AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                    AntPathRequestMatcher.antMatcher("/swagger-ui.html"))
                .permitAll());
  }
}
