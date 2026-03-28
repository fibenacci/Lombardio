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

import io.lombardio.platform.integration.api.IntegrationOutboxProperties;
import io.lombardio.platform.integration.api.IntegrationRabbitMqProperties;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({
  IntegrationOutboxProperties.class,
  IntegrationRabbitMqProperties.class
})
public class CoreConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }
}
