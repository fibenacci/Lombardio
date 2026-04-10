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

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Interface to customize security policies in a Lombardio service.
 * Used by {@link PlatformSecurityConfiguration} to build a default {@link org.springframework.security.web.SecurityFilterChain}.
 */
@FunctionalInterface
public interface SecurityPolicyCustomizer {

  /**
   * Customizes the given {@link HttpSecurity} object.
   *
   * @param http the HttpSecurity to customize
   * @throws Exception if an error occurs during configuration
   */
  void customize(HttpSecurity http) throws Exception;
}
