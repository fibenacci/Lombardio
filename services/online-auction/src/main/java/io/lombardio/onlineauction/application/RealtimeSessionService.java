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

import io.lombardio.onlineauction.api.RealtimeSessionResponse;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.RealtimeSession;
import io.lombardio.onlineauction.domain.RealtimeSessionTokenService;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;

@Service
public class RealtimeSessionService {

  private final OnlineAuctionLifecycleService lifecycleService;
  private final RealtimeSessionTokenService realtimeSessionTokenService;

  public RealtimeSessionService(
      OnlineAuctionLifecycleService lifecycleService,
      RealtimeSessionTokenService realtimeSessionTokenService) {
    this.lifecycleService = lifecycleService;
    this.realtimeSessionTokenService = realtimeSessionTokenService;
  }

  public RealtimeSessionResponse issueRealtimeSession(
      String tenantId, String auctionId, String accessToken) {
    OnlineAuction auction = lifecycleService.requirePublicAuction(tenantId, auctionId);
    BidderRegistration bidder =
        auction.registrations().stream()
            .filter(item -> bidderMatchesAccessToken(item, accessToken))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown bidder session"));
    if (bidder.approvalStatus() != BidderApprovalStatus.APPROVED) {
      throw new IllegalArgumentException("Bidder is not approved for realtime access");
    }
    if (bidder.kycStatus() != ReviewCheckStatus.PASSED
        || bidder.accountCheckStatus() != ReviewCheckStatus.PASSED) {
      throw new IllegalArgumentException("Bidder compliance checks are incomplete");
    }
    RealtimeSession session =
        realtimeSessionTokenService.createSession(bidder.paddleNumber(), auction.channelName());
    return new RealtimeSessionResponse(
        session.wsUrl(), session.channel(), session.connectionToken(), session.subscriptionToken());
  }

  private boolean bidderMatchesAccessToken(BidderRegistration bidder, String rawAccessToken) {
    String candidateHash = BidderAccessTokenHasher.sha256(rawAccessToken);
    if (bidder.accessTokenHash() != null) {
      return MessageDigest.isEqual(
          bidder.accessTokenHash().getBytes(java.nio.charset.StandardCharsets.UTF_8),
          candidateHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    return MessageDigest.isEqual(
        bidder.accessToken().getBytes(java.nio.charset.StandardCharsets.UTF_8),
        rawAccessToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
