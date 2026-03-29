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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(
    String serverUrl,
    String realm,
    String clientId,
    String adminUsername,
    String adminPassword) {}
