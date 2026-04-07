/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.loanorigination.infrastructure.support;

import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.model.LoanCase;
import io.lombardio.loanorigination.domain.model.LoanPosition;
import io.lombardio.loanorigination.domain.model.PawnTicket;
import io.lombardio.loanorigination.domain.model.PawnTicketPosition;
import io.lombardio.loanorigination.domain.model.PledgeRecord;
import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.domain.port.AmlDirectory;
import io.lombardio.loanorigination.domain.port.CustomerDirectory;
import io.lombardio.loanorigination.domain.port.KycDirectory;
import io.lombardio.loanorigination.domain.port.LoanCaseRepository;
import io.lombardio.loanorigination.domain.port.PawnTicketIssuer;
import io.lombardio.loanorigination.domain.port.PledgeRecordRepository;
import io.lombardio.loanorigination.domain.port.ValuationGuidelineRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemoryPorts {

  private InMemoryPorts() {}

  public static final class Customers implements CustomerDirectory {
    private final Map<String, CustomerProfile> store = new LinkedHashMap<>();

    public Customers() {
      save(
          new CustomerProfile(
              "customer-berlin-1",
              "tenant-default",
              "KD-1001",
              "Anna Becker",
              LocalDate.parse("1988-04-12"),
              "+49 170 111111",
              "Hauptstr. 1",
              "10115",
              "Berlin",
              "APPROVED",
              true,
              "PERSONALAUSWEIS"));
    }

    @Override
    public CustomerProfile requireById(String tenantId, String customerId) {
      CustomerProfile customer = store.get(customerId);
      if (customer == null || !customer.tenantId().equals(tenantId)) {
        throw new IllegalArgumentException("Customer not found");
      }
      return customer;
    }

    public void save(CustomerProfile customer) {
      store.put(customer.id(), customer);
    }
  }

  public static final class Kyc implements KycDirectory {
    private final Map<String, Boolean> approvals = new LinkedHashMap<>();

    public Kyc() {
      approvals.put("tenant-default:customer-berlin-1", true);
    }

    @Override
    public KycProjection getStatus(String tenantId, String customerId) {
      boolean approved = isApproved(tenantId, customerId);
      return new KycProjection(
          approved ? "APPROVED" : "NOT_STARTED", approved, approved ? "PERSONALAUSWEIS" : null);
    }

    @Override
    public boolean isApproved(String tenantId, String customerId) {
      return approvals.getOrDefault(tenantId + ":" + customerId, false);
    }

    public void setApproved(String tenantId, String customerId, boolean approved) {
      approvals.put(tenantId + ":" + customerId, approved);
    }
  }

  public static final class Aml implements AmlDirectory {
    private final Map<String, AmlAssessment> assessments = new LinkedHashMap<>();

    public Aml() {
      assessments.put(
          "tenant-default:customer-berlin-1",
          new AmlAssessment(true, true, "AML review cleared for origination"));
    }

    @Override
    public AmlAssessment assessForOrigination(
        String tenantId, String customerId, BigDecimal loanAmount) {
      return assessments.getOrDefault(
          tenantId + ":" + customerId,
          new AmlAssessment(false, true, "AML compliance feature disabled for tenant"));
    }

    public void setAssessment(String tenantId, String customerId, AmlAssessment assessment) {
      assessments.put(tenantId + ":" + customerId, assessment);
    }
  }

  public static final class Guidelines implements ValuationGuidelineRepository {
    private final Map<String, ValuationGuideline> store = new LinkedHashMap<>();

    public Guidelines() {
      save(
          new ValuationGuideline(
              "guideline-gold-585",
              "tenant-default",
              "Jewelry",
              "Gold 585",
              "Goldring 585",
              "Gelbgold 14 Karat",
              new BigDecimal("180.00")));
      save(
          new ValuationGuideline(
              "guideline-iphone-14",
              "tenant-default",
              "Electronics",
              "iPhone",
              "Apple iPhone 14 128GB",
              "gebraucht, funktionsfaehig",
              new BigDecimal("260.00")));
    }

    @Override
    public List<ValuationGuideline> findByTenantId(String tenantId) {
      return store.values().stream().filter(item -> item.tenantId().equals(tenantId)).toList();
    }

    @Override
    public Optional<ValuationGuideline> findById(String id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public ValuationGuideline save(ValuationGuideline guideline) {
      store.put(guideline.id(), guideline);
      return guideline;
    }
  }

  public static final class Loans implements LoanCaseRepository {
    private final Map<String, LoanCase> store = new LinkedHashMap<>();

    @Override
    public LoanCase save(LoanCase loanCase) {
      store.put(loanCase.id(), loanCase);
      return loanCase;
    }

    @Override
    public List<LoanCase> findByTenantId(String tenantId) {
      return store.values().stream()
          .filter(loanCase -> loanCase.tenantId().equals(tenantId))
          .toList();
    }

    @Override
    public List<LoanCase> findByTenantIdAndCustomerId(String tenantId, String customerId) {
      return store.values().stream()
          .filter(loanCase -> loanCase.tenantId().equals(tenantId))
          .filter(loanCase -> loanCase.customer().id().equals(customerId))
          .toList();
    }
  }

  public static final class PledgeRecords implements PledgeRecordRepository {
    private final Map<String, PledgeRecord> store = new LinkedHashMap<>();

    @Override
    public PledgeRecord save(PledgeRecord pledgeRecord) {
      store.put(pledgeRecord.id(), pledgeRecord);
      return pledgeRecord;
    }
  }

  public static final class PawnTickets implements PawnTicketIssuer {
    private final AtomicInteger sequence = new AtomicInteger(5000);

    @Override
    public PawnTicket issue(
        String tenantId,
        CustomerProfile customer,
        List<LoanPosition> positions,
        BigDecimal loanAmount,
        Integer termMonths,
        BigDecimal manualMonthlyOperatingFee) {
      int sequenceNumber = sequence.incrementAndGet();
      String contractNumber = "PS-" + sequenceNumber;
      return new PawnTicket(
          "ticket-1",
          contractNumber,
          contractNumber,
          contractNumber,
          "AGB-2026-03",
          "AGB text",
          Instant.parse("2026-03-18T12:00:00Z"),
          LocalDate.parse("2026-06-18"),
          LocalDate.parse("2026-07-18"),
          termMonths == null ? 3 : termMonths,
          loanAmount,
          new BigDecimal("1.00"),
          manualMonthlyOperatingFee == null ? new BigDecimal("4.50") : manualMonthlyOperatingFee,
          manualMonthlyOperatingFee == null && loanAmount.compareTo(new BigDecimal("300.00")) > 0,
          new BigDecimal("6.00"),
          new BigDecimal("13.50"),
          loanAmount.add(new BigDecimal("19.50")),
          "Legal text",
          java.util.stream.IntStream.range(0, positions.size())
              .mapToObj(
                  index ->
                      new PawnTicketPosition(
                          contractNumber + "-" + String.format("%02d", index + 1),
                          contractNumber + "-" + String.format("%02d", index + 1),
                          positions.get(index).label(),
                          positions.get(index).description(),
                          positions.get(index).pledgedValue()))
              .toList());
    }
  }
}
