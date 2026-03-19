package io.lombardio.pawnticket;

import io.lombardio.pawnticket.application.service.IssuePawnTicketCommand;
import io.lombardio.pawnticket.application.service.PawnTicketPolicyService;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PawnTicketPersistenceIntegrationTest {

    @Autowired
    private PawnTicketPolicyService pawnTicketPolicyService;

    @Autowired
    private PawnTicketRepository pawnTicketRepository;

    @Test
    void issuedTicketPersistsPositionsWithStableSortOrder() {
        var issued = pawnTicketPolicyService.issue(new IssuePawnTicketCommand(
                "tenant-default",
                "customer-anna",
                "KD-1001",
                "Anna Berg",
                "+49 40 1234567",
                List.of(
                        new PawnTicketPosition(null, null, "Ring", "585 Gold", new BigDecimal("120.00")),
                        new PawnTicketPosition(null, null, "Kette", "750 Gold", new BigDecimal("80.00"))
                ),
                new BigDecimal("200.00"),
                3,
                new BigDecimal("2.50")
        ));

        var loaded = pawnTicketRepository.findByTicketNumber(issued.ticketNumber());
        assertTrue(loaded.isPresent());
        assertEquals(2, loaded.get().positions().size());
        assertEquals("Ring", loaded.get().positions().get(0).label());
        assertEquals("Kette", loaded.get().positions().get(1).label());
        assertEquals(issued.contractNumber() + "-01", loaded.get().positions().get(0).itemNumber());
    }
}
