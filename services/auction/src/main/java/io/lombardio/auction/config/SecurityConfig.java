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
package io.lombardio.auction.config;

import io.lombardio.platform.security.SecurityPolicyCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityPolicyCustomizer securityPolicy() {
    return (http) ->
        http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/auctions/health"))
                    .permitAll()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/**"))
                    .permitAll()
                    .anyRequest()
                    .authenticated());
  }
}
