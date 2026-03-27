package io.lombardio.loanorigination.demo;

import io.lombardio.loanorigination.bootstrap.LoanOriginationDevelopmentSeeder;
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
    ApplicationRunner seedDemoData(ReferenceDataSeeder referenceDataSeeder, LoanOriginationDevelopmentSeeder scenarioDataSeeder) {
        return args -> {
            referenceDataSeeder.seed();
            scenarioDataSeeder.seed();
        };
    }
}
