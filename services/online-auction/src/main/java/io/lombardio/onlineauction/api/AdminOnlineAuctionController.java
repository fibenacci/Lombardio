package io.lombardio.onlineauction.api;

import io.lombardio.onlineauction.application.OnlineAuctionService;
import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.onlineauction.infrastructure.security.OnlineAuctionAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/online-auctions")
public class AdminOnlineAuctionController {

    private final OnlineAuctionService onlineAuctionService;
    private final OnlineAuctionAuthorizationService authorizationService;

    public AdminOnlineAuctionController(OnlineAuctionService onlineAuctionService,
                                        OnlineAuctionAuthorizationService authorizationService) {
        this.onlineAuctionService = onlineAuctionService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    List<OnlineAuctionResponse> list(@PathVariable String tenantId,
                                     @AuthenticationPrincipal AuthenticatedUser user) {
        authorizationService.requireRead(user, tenantId);
        return onlineAuctionService.listAdminAuctions(tenantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OnlineAuctionResponse create(@PathVariable String tenantId,
                                 @Valid @RequestBody CreateOnlineAuctionRequest request,
                                 @AuthenticationPrincipal AuthenticatedUser user) {
        authorizationService.requireWrite(user, tenantId);
        return onlineAuctionService.createAuction(tenantId, request);
    }

    @PostMapping("/{auctionId}/publish")
    OnlineAuctionResponse publish(@PathVariable String tenantId,
                                  @PathVariable String auctionId,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        authorizationService.requireWrite(user, tenantId);
        return onlineAuctionService.publishAuction(tenantId, auctionId);
    }

    @PostMapping("/{auctionId}/start")
    OnlineAuctionResponse start(@PathVariable String tenantId,
                                @PathVariable String auctionId,
                                @AuthenticationPrincipal AuthenticatedUser user) {
        authorizationService.requireWrite(user, tenantId);
        return onlineAuctionService.startAuction(tenantId, auctionId);
    }

    @PostMapping("/{auctionId}/close")
    OnlineAuctionResponse close(@PathVariable String tenantId,
                                @PathVariable String auctionId,
                                @AuthenticationPrincipal AuthenticatedUser user) {
        authorizationService.requireWrite(user, tenantId);
        return onlineAuctionService.closeAuction(tenantId, auctionId);
    }

    @PostMapping("/{auctionId}/registrations/{registrationId}/review")
    OnlineAuctionResponse reviewRegistration(@PathVariable String tenantId,
                                             @PathVariable String auctionId,
                                             @PathVariable String registrationId,
                                             @Valid @RequestBody BidderReviewRequest request,
                                             @AuthenticationPrincipal AuthenticatedUser user) {
        authorizationService.requireWrite(user, tenantId);
        return onlineAuctionService.reviewRegistration(tenantId, auctionId, registrationId, request);
    }
}
