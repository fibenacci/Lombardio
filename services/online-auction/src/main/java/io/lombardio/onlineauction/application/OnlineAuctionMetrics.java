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
package io.lombardio.onlineauction.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.Locale;

public class OnlineAuctionMetrics {

  private final MeterRegistry meterRegistry;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "MeterRegistry is a managed metrics dependency that must be retained directly")
  public OnlineAuctionMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public static OnlineAuctionMetrics noop() {
    return new OnlineAuctionMetrics(new SimpleMeterRegistry());
  }

  public void recordAuctionCreated() {
    meterRegistry.counter("lombardio.online_auction.created").increment();
  }

  public void recordBidderRegistration() {
    meterRegistry.counter("lombardio.online_auction.bidder_registrations").increment();
  }

  public void recordBidderReview(
      BidderApprovalStatus approvalStatus,
      ReviewCheckStatus kycStatus,
      ReviewCheckStatus accountCheckStatus) {
    String decision = approvalStatus.name().toLowerCase(Locale.ROOT);
    String kyc = kycStatus.name().toLowerCase(Locale.ROOT);
    String account = accountCheckStatus.name().toLowerCase(Locale.ROOT);
    meterRegistry
        .counter(
            "lombardio.online_auction.bidder_reviews",
            "decision",
            decision,
            "kyc",
            kyc,
            "account",
            account)
        .increment();
  }

  public void recordBidPlaced(BigDecimal amount) {
    meterRegistry.counter("lombardio.online_auction.bids_placed").increment();
    DistributionSummary.builder("lombardio.online_auction.bid_amount")
        .baseUnit("eur")
        .register(meterRegistry)
        .record(amount.doubleValue());
  }
}
