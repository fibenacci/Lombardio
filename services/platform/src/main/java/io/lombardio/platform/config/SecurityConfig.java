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
import io.lombardio.platform.security.SecurityPolicyCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({OperatorSessionProperties.class, OperatorBffProperties.class})
public class SecurityConfig {

  private final StoredOperatorSessionService storedOperatorSessionService;
  private final OperatorSessionProperties operatorSessionProperties;

  public SecurityConfig(
      StoredOperatorSessionService storedOperatorSessionService,
      OperatorSessionProperties operatorSessionProperties) {
    this.storedOperatorSessionService = storedOperatorSessionService;
    this.operatorSessionProperties = operatorSessionProperties;
  }

  @Bean
  public SecurityPolicyCustomizer securityPolicy() {
    return (http) -> {
      // 3. Apply BFF specific CSRF protection
      http.csrf(
          csrf ->
              csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                  .ignoringRequestMatchers(
                      request -> request.getHeader("Authorization") != null,
                      AntPathRequestMatcher.antMatcher("/api/v1/platform/health"),
                      AntPathRequestMatcher.antMatcher("/api/v1/platform/auth/login"),
                      AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                      AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                      AntPathRequestMatcher.antMatcher("/actuator/**"))
                  .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

      // 4. Apply service-specific path rules
      http.authorizeHttpRequests(
          auth ->
              auth.requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/platform/health"))
                  .permitAll()
                  .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/platform/auth/login"))
                  .permitAll()
                  .requestMatchers(
                      AntPathRequestMatcher.antMatcher("/api/v1/platform/auth/refresh"),
                      AntPathRequestMatcher.antMatcher("/api/v1/platform/auth/logout"),
                      AntPathRequestMatcher.antMatcher("/api/v1/platform/auth/me"))
                  .authenticated()
                  .requestMatchers(
                      AntPathRequestMatcher.antMatcher("/actuator/health"),
                      AntPathRequestMatcher.antMatcher("/actuator/info"))
                  .permitAll()
                  .anyRequest()
                  .authenticated());

      // 5. Add BFF specific session filter
      http.addFilterBefore(
          new OperatorSessionAuthenticationFilter(
              storedOperatorSessionService, operatorSessionProperties),
          BearerTokenAuthenticationFilter.class);
    };
  }
}
