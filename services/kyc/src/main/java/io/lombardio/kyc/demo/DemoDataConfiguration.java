package io.lombardio.kyc.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoDataProperties.class)
class DemoDataConfiguration {

    @Bean
    @ConditionalOnProperty(value = "demo.data.enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner seedDemoData(ScenarioDataSeeder scenarioDataSeeder) {
        return args -> scenarioDataSeeder.seed();
    }
}
