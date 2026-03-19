package io.lombardio.loanorigination.bootstrap;

import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.infrastructure.persistence.adapter.ValuationGuidelinePersistenceAdapter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class LoanOriginationDevelopmentSeeder {

    @Bean
    ApplicationRunner seedGuidelines(ValuationGuidelinePersistenceAdapter repository) {
        return args -> {
            repository.save(new ValuationGuideline("guideline-gold-585", "tenant-default", "Jewelry", "Gold 585", "Goldring 585", "Gelbgold 14 Karat", new BigDecimal("180.00")));
            repository.save(new ValuationGuideline("guideline-gold-750", "tenant-default", "Jewelry", "Gold 750", "Goldkette 750", "Gelbgold 18 Karat", new BigDecimal("320.00")));
            repository.save(new ValuationGuideline("guideline-silver-925", "tenant-default", "Jewelry", "Silver 925", "Silberarmband 925", "Sterlingsilber", new BigDecimal("45.00")));
            repository.save(new ValuationGuideline("guideline-iphone-14", "tenant-default", "Electronics", "iPhone", "Apple iPhone 14 128GB", "gebraucht, funktionsfaehig", new BigDecimal("260.00")));
        };
    }
}
