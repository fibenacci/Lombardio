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
package io.lombardio.pawnticket.config;

import io.lombardio.pawnticket.security.InternalServiceAuthenticationFilter;
import io.lombardio.platform.security.SecurityPolicyCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${internal.service-token}")
  private String internalServiceToken;

  @Bean
  public SecurityPolicyCustomizer securityPolicy() {
    return (http) -> {
      http.authorizeHttpRequests(
              auth ->
                  auth.requestMatchers(
                          AntPathRequestMatcher.antMatcher("/api/v1/pawn-tickets/health"))
                      .permitAll()
                      .requestMatchers(AntPathRequestMatcher.antMatcher("/api/internal/**"))
                      .hasAuthority(InternalServiceAuthenticationFilter.INTERNAL_SERVICE_AUTHORITY)
                      .requestMatchers(
                          AntPathRequestMatcher.antMatcher("/actuator/health"),
                          AntPathRequestMatcher.antMatcher("/actuator/info"))
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .addFilterBefore(
              new InternalServiceAuthenticationFilter(internalServiceToken),
              BearerTokenAuthenticationFilter.class);
    };
  }
}
