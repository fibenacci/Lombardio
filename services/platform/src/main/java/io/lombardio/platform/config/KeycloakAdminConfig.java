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

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakAdminConfig {

  @Bean
  public Keycloak keycloak(KeycloakProperties props) {
    KeycloakBuilder builder =
        KeycloakBuilder.builder()
            .serverUrl(props.serverUrl())
            .realm(props.realm())
            .clientId(props.clientId())
            .username(props.adminUsername())
            .password(props.adminPassword())
            .grantType("password");

    return builder.build();
  }

  @Bean
  public RestClient keycloakOidcRestClient(KeycloakProperties props) {
    return RestClient.builder().baseUrl(props.serverUrl()).build();
  }
}
