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
package io.lombardio.pawnticket.infrastructure.persistence.support;

import io.lombardio.pawnticket.domain.model.CashTransaction;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.port.CashTransactionRepository;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryRepositories {

  private InMemoryRepositories() {}

  public static final class PawnTickets implements PawnTicketRepository {
    private final Map<String, PawnTicket> store = new LinkedHashMap<>();
    private final Map<String, PawnTicket> byTicketNumber = new LinkedHashMap<>();

    @Override
    public PawnTicket save(PawnTicket pawnTicket) {
      store.put(pawnTicket.id(), pawnTicket);
      byTicketNumber.put(pawnTicket.ticketNumber(), pawnTicket);
      return pawnTicket;
    }

    @Override
    public Optional<PawnTicket> findByTicketNumber(String ticketNumber) {
      return Optional.ofNullable(byTicketNumber.get(ticketNumber));
    }

    @Override
    public List<PawnTicket> findByTenantId(String tenantId) {
      return store.values().stream()
          .filter(pawnTicket -> pawnTicket.tenantId().equals(tenantId))
          .sorted(Comparator.comparing(PawnTicket::createdAt).reversed())
          .toList();
    }

    @Override
    public List<PawnTicket> findByTenantIdAndCustomerId(String tenantId, String customerId) {
      return store.values().stream()
          .filter(pawnTicket -> pawnTicket.tenantId().equals(tenantId))
          .filter(pawnTicket -> pawnTicket.customerId().equals(customerId))
          .sorted(Comparator.comparing(PawnTicket::createdAt).reversed())
          .toList();
    }
  }

  public static final class CashTransactions implements CashTransactionRepository {
    private final Map<String, CashTransaction> store = new LinkedHashMap<>();

    @Override
    public CashTransaction save(CashTransaction cashTransaction) {
      store.put(cashTransaction.id(), cashTransaction);
      return cashTransaction;
    }

    @Override
    public List<CashTransaction> findByTenantId(String tenantId) {
      return store.values().stream()
          .filter(transaction -> transaction.tenantId().equals(tenantId))
          .sorted(Comparator.comparing(CashTransaction::createdAt).reversed())
          .toList();
    }
  }
}
