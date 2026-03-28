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
package io.lombardio.auction.api.http;

import io.lombardio.auction.domain.model.AuctionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AuctionResponse(
    String id,
    String title,
    String location,
    AuctionStatus status,
    LocalDate publicAnnouncementDate,
    LocalDate auctionDate,
    Instant liveStartedAt,
    Instant closedAt,
    String announcementReference,
    List<AuctionLotResponse> lots) {}
