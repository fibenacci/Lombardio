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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties({LombardioSecurityProperties.class, LombardioCorsProperties.class})
public class PlatformSecurityConfiguration {

  @Bean
  @ConditionalOnMissingBean(AuditService.class)
  public AuditService auditService() {
    return new Slf4jAuditService();
  }

  @Bean
  public AuditAspect auditAspect(AuditService auditService) {
    return new AuditAspect(auditService);
  }

  @Bean
  @ConditionalOnMissingBean
  public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder(
      LombardioSecurityProperties properties,
      org.springframework.beans.factory.ObjectProvider<org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties> springPropertiesProvider) {
    String uri = properties.jwkSetUri();
    if (uri == null || uri.isBlank()) {
      org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties springProperties = springPropertiesProvider.getIfAvailable();
      if (springProperties != null && springProperties.getJwt() != null) {
        uri = springProperties.getJwt().getJwkSetUri();
      }
    }

    if (uri == null || uri.isBlank()) {
      // Return a dummy decoder for tests if no URI is provided
      // This prevents context load failure in non-web tests that still pick up SecurityConfig
      return token -> {
        throw new org.springframework.security.oauth2.jwt.JwtException("No JwtDecoder configured");
      };
    }

    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(uri).build();
    jwtDecoder.setJwtValidator(
        KeycloakJwtValidators.operatorAccessTokenValidator(properties.operatorClientId()));
    return jwtDecoder;
  }

  @Bean
  @ConditionalOnMissingBean
  public CorsConfigurationSource corsConfigurationSource(LombardioCorsProperties corsProperties) {
    if (corsProperties.allowedOrigins().isEmpty()) {
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

  @Bean
  @ConditionalOnMissingBean
  public LombardioSecurityConfigurer lombardioSecurityConfigurer(
      LombardioSecurityProperties properties, CorsConfigurationSource corsConfigurationSource) {
    return new LombardioSecurityConfigurer(properties, corsConfigurationSource);
  }

  @Bean
  @ConditionalOnProperty(name = "lombardio.security.default-filter-chain.enabled", matchIfMissing = true)
  @ConditionalOnMissingBean(org.springframework.security.web.SecurityFilterChain.class)
  public org.springframework.security.web.SecurityFilterChain defaultSecurityFilterChain(
      org.springframework.security.config.annotation.web.builders.HttpSecurity http,
      LombardioSecurityConfigurer securityConfigurer,
      SecurityPolicyCustomizer customizer)
      throws Exception {
    securityConfigurer.configureStatelessDefaults(http);
    securityConfigurer.configureOpenApiExemptions(http);
    customizer.customize(http);
    return http.build();
  }
}
