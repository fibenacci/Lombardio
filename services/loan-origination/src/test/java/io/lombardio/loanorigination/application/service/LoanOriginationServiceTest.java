package io.lombardio.loanorigination.application.service;

import io.lombardio.loanorigination.domain.port.AmlDirectory;
import io.lombardio.loanorigination.infrastructure.support.InMemoryPorts;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanOriginationServiceTest {

    private final InMemoryPorts.Kyc kyc = new InMemoryPorts.Kyc();
    private final InMemoryPorts.Aml aml = new InMemoryPorts.Aml();

    private final LoanOriginationService service = new LoanOriginationService(
            new InMemoryPorts.Customers(),
            kyc,
            aml,
            new InMemoryPorts.Guidelines(),
            new InMemoryPorts.Loans(),
            new InMemoryPorts.PawnTickets(),
            Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void shouldCreateLoanForExistingCustomerAndDefaultToGuidelineValue() {
        var loan = service.createLoan("tenant-default", new CreateLoanCommand(
                "customer-berlin-1",
                List.of(new CreateLoanPositionCommand(
                        1,
                        "Goldring",
                        "Ring mit Gravur",
                        "guideline-gold-585",
                        null
                )),
                3,
                null,
                false,
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals("Anna Becker", loan.customer().displayName());
        assertEquals(new BigDecimal("180.00"), loan.positions().get(0).pledgedValue());
        assertEquals(new BigDecimal("180.00"), loan.pawnTickets().get(0).totalLoanValue());
        assertEquals("de", loan.pledgeRecord().languageCode());
        assertEquals("PERSONALAUSWEIS", loan.pledgeRecord().checkedDocumentType());
    }

    @Test
    void shouldAllowManualLoanValueOverride() {
        var loan = service.createLoan("tenant-default", new CreateLoanCommand(
                "customer-berlin-1",
                List.of(new CreateLoanPositionCommand(
                        1,
                        "iPhone",
                        "iPhone 14 blau",
                        "guideline-iphone-14",
                        new BigDecimal("300.00")
                )),
                3,
                null,
                false,
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals("Anna Becker", loan.customer().displayName());
        assertEquals(new BigDecimal("300.00"), loan.positions().get(0).pledgedValue());
        assertEquals(new BigDecimal("300.00"), loan.pawnTickets().get(0).totalLoanValue());
        assertEquals("2026-06-18", loan.pawnTickets().get(0).dueDate().toString());
    }

    @Test
    void shouldSplitPositionsIntoSeparatePawnTicketsByTicketGroup() {
        var loan = service.createLoan("tenant-default", new CreateLoanCommand(
                "customer-berlin-1",
                List.of(
                        new CreateLoanPositionCommand(1, "Goldring", "Ring mit Gravur", "guideline-gold-585", new BigDecimal("180.00")),
                        new CreateLoanPositionCommand(2, "iPhone", "iPhone 14 blau", "guideline-iphone-14", new BigDecimal("300.00"))
                ),
                3,
                null,
                false,
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals(2, loan.pawnTickets().size());
        assertEquals("PS-5001", loan.pawnTickets().get(0).ticketNumber());
        assertEquals("PS-5002", loan.pawnTickets().get(1).ticketNumber());
        assertEquals(Integer.valueOf(1), loan.positions().get(0).ticketGroup());
        assertEquals(Integer.valueOf(2), loan.positions().get(1).ticketGroup());
    }

    @Test
    void shouldRejectLoanWhenKycIsNotApproved() {
        kyc.setApproved("tenant-default", "customer-berlin-1", false);

        var exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.createLoan("tenant-default", new CreateLoanCommand(
                        "customer-berlin-1",
                        List.of(new CreateLoanPositionCommand(1, "Goldring", "Ring mit Gravur", "guideline-gold-585", new BigDecimal("180.00"))),
                        3,
                        null,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals("KYC approval required before loan origination", exception.getMessage());
    }

    @Test
    void shouldRejectLoanWhenAmlBlocksOrigination() {
        aml.setAssessment("tenant-default", "customer-berlin-1", new AmlDirectory.AmlAssessment(
                true,
                false,
                "AML review required before loan origination"
        ));

        var exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.createLoan("tenant-default", new CreateLoanCommand(
                        "customer-berlin-1",
                        List.of(new CreateLoanPositionCommand(1, "Goldring", "Ring mit Gravur", "guideline-gold-585", new BigDecimal("180.00"))),
                        3,
                        null,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals("AML review required before loan origination", exception.getMessage());
    }

    @Test
    void shouldRequirePowerOfAttorneyForThirdPartyPresentation() {
        var exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.createLoan("tenant-default", new CreateLoanCommand(
                        "customer-berlin-1",
                        List.of(new CreateLoanPositionCommand(1, "Goldring", "Ring mit Gravur", "guideline-gold-585", new BigDecimal("180.00"))),
                        3,
                        null,
                        true,
                        "Max Beispiel",
                        "Nebenweg 4",
                        "10117",
                        "Berlin",
                        null
                ))
        );

        assertTrue(exception.getMessage().contains("powerOfAttorneyDocumentDataUrl"));
    }

    @Test
    void shouldCreatePledgeRecordForThirdPartyPresentation() {
        var loan = service.createLoan("tenant-default", new CreateLoanCommand(
                "customer-berlin-1",
                List.of(new CreateLoanPositionCommand(1, "Goldring", "Ring mit Gravur", "guideline-gold-585", new BigDecimal("180.00"))),
                3,
                null,
                true,
                "Max Beispiel",
                "Nebenweg 4",
                "10117",
                "Berlin",
                "data:application/pdf;base64,AAA"
        ));

        assertTrue(loan.pledgeRecord().powerOfAttorneyRequired());
        assertEquals("Max Beispiel", loan.pledgeRecord().bearerName());
        assertEquals("2030-03-18", loan.pledgeRecord().retentionUntil().toString());
    }

    @Test
    void shouldListLoansForCustomerInReverseChronologicalOrder() {
        service.createLoan("tenant-default", new CreateLoanCommand(
                "customer-berlin-1",
                List.of(new CreateLoanPositionCommand(1, "Goldring", "Ring mit Gravur", "guideline-gold-585", new BigDecimal("180.00"))),
                3,
                null,
                false,
                null,
                null,
                null,
                null,
                null
        ));

        var loans = service.listLoans("tenant-default", "customer-berlin-1");

        assertEquals(1, loans.size());
        assertEquals("Anna Becker", loans.get(0).customer().displayName());
        assertEquals("de", loans.get(0).pledgeRecord().languageCode());
    }
}
