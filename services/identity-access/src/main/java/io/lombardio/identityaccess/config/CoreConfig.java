package io.lombardio.identityaccess.config;

import io.lombardio.identityaccess.auth.application.SecretCipher;
import io.lombardio.identityaccess.auth.application.TotpCodeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
public class CoreConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    TotpCodeService totpCodeService(Clock clock) {
        return new TotpCodeService(clock);
    }

    @Bean
    SecretCipher secretCipher(@Value("${identity.security.encryption-key}") String encryptionKey) {
        return new SecretCipher(encryptionKey);
    }
}
