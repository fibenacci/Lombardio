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
package io.lombardio.platform.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoDataProperties.class)
class DemoDataConfiguration {

  @Bean
  @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
      value = "demo.data.enabled",
      havingValue = "true")
  ApplicationRunner seedDemoData(ScenarioDataSeeder scenarioDataSeeder) {
    return args -> scenarioDataSeeder.seed();
  }
}
