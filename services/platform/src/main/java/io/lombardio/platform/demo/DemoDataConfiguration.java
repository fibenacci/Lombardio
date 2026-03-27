package io.lombardio.platform.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoDataProperties.class)
class DemoDataConfiguration {

    @Bean
    // @ConditionalOnProperty(value = "demo.data.enabled", havingValue = "true") // Kommentiert aus, um die Demo-Daten-Erstellung zu deaktivieren
    ApplicationRunner seedDemoData(ScenarioDataSeeder scenarioDataSeeder) {
        return args -> scenarioDataSeeder.seed();
    }
}

