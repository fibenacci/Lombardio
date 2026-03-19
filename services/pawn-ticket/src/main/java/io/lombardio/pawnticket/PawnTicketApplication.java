package io.lombardio.pawnticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PawnTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(PawnTicketApplication.class, args);
    }
}
