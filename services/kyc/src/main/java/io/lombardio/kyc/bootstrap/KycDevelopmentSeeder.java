package io.lombardio.kyc.bootstrap;

import io.lombardio.kyc.domain.KycRecord;
import io.lombardio.kyc.domain.KycRepository;
import io.lombardio.kyc.domain.KycStatus;
import io.lombardio.kyc.domain.KycVerificationMode;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class KycDevelopmentSeeder {

    @Bean
    ApplicationRunner seedKycData(KycRepository kycRepository) {
        return args -> kycRepository.save(new KycRecord(
                "kyc-1",
                "tenant-default",
                "customer-berlin-1",
                KycVerificationMode.MANUAL,
                KycStatus.APPROVED,
                LocalDate.now().plusYears(1),
                "PERSONALAUSWEIS",
                "L01X00T47",
                LocalDate.now().plusYears(4),
                "data:image/png;base64,ZXhhbXBsZS1mcm9udA==",
                "data:image/png;base64,ZXhhbXBsZS1iYWNr",
                "Identitaet geprueft",
                null,
                null,
                null
        ));
    }
}
