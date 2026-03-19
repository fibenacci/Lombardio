package io.lombardio.customer.demo;

import io.lombardio.customer.domain.model.Customer;
import io.lombardio.customer.domain.port.CustomerRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
class ScenarioDataSeeder {

    private record DemoTenant(String id, String key, String numberPrefix, String city, String postalCode) {
    }

    private static final List<DemoTenant> TENANTS = List.of(
            new DemoTenant("tenant-default", "default", "BER", "Berlin", "10115"),
            new DemoTenant("tenant-hamburg", "hanseatic", "HAM", "Hamburg", "20095"),
            new DemoTenant("tenant-munich", "isar", "MUC", "Muenchen", "80331"),
            new DemoTenant("tenant-cologne", "rhein", "CGN", "Koeln", "50667"),
            new DemoTenant("tenant-stuttgart", "neckar", "STR", "Stuttgart", "70173")
    );

    private static final String[] FIRST_NAMES = {"Anna", "Murat", "Leonie", "Jonas", "Sofia", "Mila", "Emre", "Paul", "Nina", "David", "Lina", "Felix", "Aylin", "Noah", "Mara", "Yusuf"};
    private static final String[] LAST_NAMES = {"Becker", "Yilmaz", "Schmidt", "Kaya", "Wagner", "Hartmann", "Keller", "Nguyen", "Fischer", "Ali", "Scholz", "Krause", "Demir", "Walter", "Schuster", "Brandt"};
    private static final String[] STREETS = {"Hauptstrasse", "Marktstrasse", "Bergweg", "Lindenallee", "Bahnhofstrasse", "Parkring", "Feldweg", "Muehlenstrasse"};

    private final CustomerRepository customerRepository;
    private final DemoDataProperties demoDataProperties;

    ScenarioDataSeeder(CustomerRepository customerRepository, DemoDataProperties demoDataProperties) {
        this.customerRepository = customerRepository;
        this.demoDataProperties = demoDataProperties;
    }

    void seed() {
        int tenantCount = tenantCount(demoDataProperties.effectiveScale());
        int customersPerTenant = customersPerTenant(demoDataProperties.effectiveScale());

        for (int tenantIndex = 0; tenantIndex < tenantCount; tenantIndex++) {
            DemoTenant tenant = TENANTS.get(tenantIndex);
            for (int customerIndex = 1; customerIndex <= customersPerTenant; customerIndex++) {
                customerRepository.save(buildCustomer(tenant, tenantIndex, customerIndex));
            }
        }
    }

    private Customer buildCustomer(DemoTenant tenant, int tenantIndex, int customerIndex) {
        String firstName = FIRST_NAMES[(customerIndex + tenantIndex) % FIRST_NAMES.length];
        String lastName = LAST_NAMES[(customerIndex * 2 + tenantIndex) % LAST_NAMES.length];
        String email = customerIndex % 5 == 0
                ? null
                : (firstName + "." + lastName + "." + tenant.key() + customerIndex + "@demo.lombardio.local").toLowerCase();
        String onlineAccessStatus = switch (customerIndex % 6) {
            case 0, 3 -> "ACTIVE";
            case 1 -> "INVITED";
            default -> "NOT_REQUESTED";
        };

        return new Customer(
                "customer-" + tenant.key() + "-" + String.format("%04d", customerIndex),
                tenant.id(),
                tenant.numberPrefix() + "-" + String.format("%04d", 1000 + customerIndex),
                firstName,
                lastName,
                LocalDate.of(1965 + ((customerIndex + tenantIndex) % 35), ((customerIndex - 1) % 12) + 1, ((customerIndex - 1) % 27) + 1),
                "+49 1" + String.format("%02d", 50 + tenantIndex) + " " + String.format("%06d", 100000 + customerIndex),
                email,
                customerIndex % 3 != 0,
                onlineAccessStatus,
                STREETS[(customerIndex + tenantIndex) % STREETS.length] + " " + (10 + customerIndex),
                tenant.postalCode(),
                tenant.city()
        );
    }

    private int tenantCount(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 2;
            case "large" -> TENANTS.size();
            default -> 4;
        };
    }

    private int customersPerTenant(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 12;
            case "large" -> 90;
            default -> 36;
        };
    }

    private String normalize(String scale) {
        return scale == null ? "medium" : scale.trim().toLowerCase();
    }
}
