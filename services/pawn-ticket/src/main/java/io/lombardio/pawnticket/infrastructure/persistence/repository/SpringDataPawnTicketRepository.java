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
package io.lombardio.pawnticket.infrastructure.persistence.repository;

import io.lombardio.pawnticket.infrastructure.persistence.entity.PawnTicketEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPawnTicketRepository extends JpaRepository<PawnTicketEntity, String> {

  @EntityGraph(attributePaths = "positions")
  Optional<PawnTicketEntity> findByTicketNumber(String ticketNumber);

  @EntityGraph(attributePaths = "positions")
  List<PawnTicketEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

  @EntityGraph(attributePaths = "positions")
  List<PawnTicketEntity> findByTenantIdAndCustomerIdOrderByCreatedAtDesc(
      String tenantId, String customerId);
}
