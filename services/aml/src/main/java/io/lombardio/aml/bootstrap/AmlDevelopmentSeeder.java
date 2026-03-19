package io.lombardio.aml.bootstrap;

import io.lombardio.aml.domain.model.AmlCase;
import io.lombardio.aml.domain.model.AmlRiskLevel;
import io.lombardio.aml.domain.model.AmlStatus;
import io.lombardio.aml.domain.port.AmlRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class AmlDevelopmentSeeder {

    @Bean
    ApplicationRunner seedAmlData(AmlRepository amlRepository) {
        return args -> {
            Instant now = Instant.parse("2026-03-18T00:00:00Z");
            amlRepository.save(new AmlCase(
                    "aml-customer-berlin-1",
                    "tenant-default",
                    "customer-berlin-1",
                    AmlStatus.CLEAR,
                    AmlRiskLevel.LOW,
                    false,
                    false,
                    false,
                    true,
                    false,
                    null,
                    "Initial AML review cleared for development tenant",
                    now,
                    now,
                    now
            ));
            amlRepository.save(new AmlCase(
                    "aml-customer-berlin-2",
                    "tenant-default",
                    "customer-berlin-2",
                    AmlStatus.REVIEW_REQUIRED,
                    AmlRiskLevel.MEDIUM,
                    false,
                    false,
                    true,
                    false,
                    false,
                    null,
                    "Manual AML review pending",
                    now,
                    null,
                    now
            ));
        };
    }
}
