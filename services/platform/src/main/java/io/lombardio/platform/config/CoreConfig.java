package io.lombardio.platform.config;

import io.lombardio.platform.integration.api.IntegrationOutboxProperties;
import io.lombardio.platform.integration.api.IntegrationRabbitMqProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({IntegrationOutboxProperties.class, IntegrationRabbitMqProperties.class})
public class CoreConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
