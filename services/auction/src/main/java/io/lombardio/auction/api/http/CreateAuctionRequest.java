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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateAuctionRequest(
    @NotBlank String title,
    @NotBlank String location,
    @NotEmpty List<@Valid AuctionLotRequest> lots) {

  public CreateAuctionRequest {
    lots = List.copyOf(lots == null ? List.of() : lots);
  }

  @Override
  public List<AuctionLotRequest> lots() {
    return List.copyOf(lots);
  }
}
