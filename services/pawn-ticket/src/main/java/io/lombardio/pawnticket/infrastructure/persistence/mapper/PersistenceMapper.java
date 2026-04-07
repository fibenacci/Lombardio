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
package io.lombardio.pawnticket.infrastructure.persistence.mapper;

import io.lombardio.pawnticket.domain.model.CashTransaction;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.pawnticket.infrastructure.persistence.entity.CashTransactionEntity;
import io.lombardio.pawnticket.infrastructure.persistence.entity.PawnTicketEntity;
import io.lombardio.pawnticket.infrastructure.persistence.entity.PawnTicketPositionEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PersistenceMapper {

  // PawnTicket mapping
  @Mapping(target = "positions", ignore = true)
  PawnTicketEntity toEntity(PawnTicket domain);

  PawnTicket toDomain(PawnTicketEntity entity);

  // CashTransaction mapping
  CashTransactionEntity toEntity(CashTransaction domain);

  CashTransaction toDomain(CashTransactionEntity entity);

  // Position mapping
  @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
  @Mapping(target = "pawnTicket", ignore = true)
  @Mapping(target = "sortOrder", ignore = true)
  PawnTicketPositionEntity toPositionEntity(PawnTicketPosition domain);

  PawnTicketPosition toPositionDomain(PawnTicketPositionEntity entity);

  @AfterMapping
  default void linkPositions(@MappingTarget PawnTicketEntity entity, PawnTicket domain) {
    if (domain.positions() != null) {
      for (int i = 0; i < domain.positions().size(); i++) {
        PawnTicketPositionEntity positionEntity = toPositionEntity(domain.positions().get(i));
        positionEntity.setPawnTicket(entity);
        positionEntity.setSortOrder(i);
        entity.getPositions().add(positionEntity);
      }
    }
  }
}
