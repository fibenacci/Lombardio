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
package io.lombardio.pawnticket.api.http.mapper;

import io.lombardio.pawnticket.api.http.CashTransactionResponse;
import io.lombardio.pawnticket.api.http.ExecuteCashTransactionRequest;
import io.lombardio.pawnticket.api.http.IssuePawnTicketRequest;
import io.lombardio.pawnticket.api.http.PawnTicketOverviewResponse;
import io.lombardio.pawnticket.api.http.PawnTicketPositionPayload;
import io.lombardio.pawnticket.api.http.PawnTicketPositionResponse;
import io.lombardio.pawnticket.api.http.PawnTicketQuoteRequest;
import io.lombardio.pawnticket.api.http.PawnTicketResponse;
import io.lombardio.pawnticket.api.http.SettlementResponse;
import io.lombardio.pawnticket.application.service.ExecuteCashTransactionCommand;
import io.lombardio.pawnticket.application.service.IssuePawnTicketCommand;
import io.lombardio.pawnticket.application.service.PawnTicketQuoteCommand;
import io.lombardio.pawnticket.application.service.PawnTicketSettlementResult;
import io.lombardio.pawnticket.domain.model.CashTransaction;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiMapper {

  @Mapping(target = "totalLoanValue", source = "loanAmount")
  PawnTicketResponse toPawnTicketResponse(PawnTicket domain);

  @Mapping(target = "totalLoanValue", source = "loanAmount")
  @Mapping(target = "positionCount", expression = "java(domain.positions().size())")
  PawnTicketOverviewResponse toOverviewResponse(PawnTicket domain);

  PawnTicketPositionResponse toPositionResponse(PawnTicketPosition domain);

  SettlementResponse toSettlementResponse(PawnTicketSettlementResult domain);

  CashTransactionResponse toCashTransactionResponse(CashTransaction domain);

  @Mapping(target = "itemNumber", ignore = true)
  @Mapping(target = "itemBarcode", ignore = true)
  PawnTicketPosition toPositionDomain(PawnTicketPositionPayload payload);

  PawnTicketQuoteCommand toQuoteCommand(PawnTicketQuoteRequest request);

  @Mapping(target = "manualMonthlyOperatingFee", source = "manualMonthlyOperatingFee")
  IssuePawnTicketCommand toIssueCommand(IssuePawnTicketRequest request);

  ExecuteCashTransactionCommand toExecuteCashTransactionCommand(
      ExecuteCashTransactionRequest request);
}
