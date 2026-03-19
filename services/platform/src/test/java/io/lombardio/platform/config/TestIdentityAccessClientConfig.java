package io.lombardio.platform.config;

import io.lombardio.platform.security.IdentityAccessClient;
import io.lombardio.platform.security.IdentityCurrentUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Optional;

@TestConfiguration
public class TestIdentityAccessClientConfig {

    @Bean
    @Primary
    IdentityAccessClient identityAccessClient() {
        return token -> switch (token) {
            case "platform-read-token" -> Optional.of(new IdentityCurrentUser(
                    "user-platform-admin",
                    "user-platform-admin",
                    "tenant-platform",
                    false,
                    List.of("platform.tenants.read")
            ));
            case "platform-write-token" -> Optional.of(new IdentityCurrentUser(
                    "user-platform-admin",
                    "user-platform-admin",
                    "tenant-platform",
                    false,
                    List.of("platform.tenants.read", "platform.tenants.write")
            ));
            case "delegated-platform-token" -> Optional.of(new IdentityCurrentUser(
                    "user-admin",
                    "user-platform-admin",
                    "tenant-default",
                    true,
                    List.of("platform.tenants.read", "platform.tenants.write")
            ));
            case "tenant-admin-token" -> Optional.of(new IdentityCurrentUser(
                    "user-admin",
                    "user-admin",
                    "tenant-default",
                    false,
                    List.of("users.read", "users.write")
            ));
            default -> Optional.empty();
        };
    }
}
