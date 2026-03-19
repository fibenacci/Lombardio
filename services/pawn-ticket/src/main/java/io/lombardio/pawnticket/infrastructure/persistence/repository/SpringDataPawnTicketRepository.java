package io.lombardio.pawnticket.infrastructure.persistence.repository;

import io.lombardio.pawnticket.infrastructure.persistence.entity.PawnTicketEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataPawnTicketRepository extends JpaRepository<PawnTicketEntity, String> {

    @EntityGraph(attributePaths = "positions")
    Optional<PawnTicketEntity> findByTicketNumber(String ticketNumber);

    @EntityGraph(attributePaths = "positions")
    List<PawnTicketEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    @EntityGraph(attributePaths = "positions")
    List<PawnTicketEntity> findByTenantIdAndCustomerIdOrderByCreatedAtDesc(String tenantId, String customerId);
}
