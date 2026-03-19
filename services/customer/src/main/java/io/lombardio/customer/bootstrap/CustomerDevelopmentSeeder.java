package io.lombardio.customer.bootstrap;

import io.lombardio.customer.domain.model.Customer;
import io.lombardio.customer.domain.port.CustomerRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class CustomerDevelopmentSeeder {

    @Bean
    ApplicationRunner seedCustomerData(CustomerRepository customerRepository) {
        return args -> {
            customerRepository.save(new Customer(
                    "customer-berlin-1",
                    "tenant-default",
                    "KD-1001",
                    "Anna",
                    "Becker",
                    LocalDate.parse("1988-04-12"),
                    "+49 170 111111",
                    "Hauptstr. 1",
                    "10115",
                    "Berlin"
            ));

            customerRepository.save(new Customer(
                    "customer-berlin-2",
                    "tenant-default",
                    "KD-1002",
                    "Murat",
                    "Yilmaz",
                    LocalDate.parse("1985-09-03"),
                    "+49 170 222222",
                    "Brunnenstr. 20",
                    "10119",
                    "Berlin"
            ));
        };
    }
}
