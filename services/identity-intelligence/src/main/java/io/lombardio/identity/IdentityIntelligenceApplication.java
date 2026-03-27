package io.lombardio.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class IdentityIntelligenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityIntelligenceApplication.class, args);
    }
}
