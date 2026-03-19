package io.lombardio.onlineauction.api;

import io.lombardio.onlineauction.application.OnlineAuctionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/tenants/{tenantId}/online-auctions")
public class PublicOnlineAuctionController {

    private final OnlineAuctionService onlineAuctionService;

    public PublicOnlineAuctionController(OnlineAuctionService onlineAuctionService) {
        this.onlineAuctionService = onlineAuctionService;
    }

    @GetMapping
    List<OnlineAuctionResponse> list(@PathVariable String tenantId) {
        return onlineAuctionService.listPublicAuctions(tenantId);
    }

    @GetMapping("/{auctionId}")
    OnlineAuctionResponse get(@PathVariable String tenantId,
                              @PathVariable String auctionId) {
        return onlineAuctionService.getPublicAuction(tenantId, auctionId);
    }

    @PostMapping("/{auctionId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    BidderRegistrationResponse register(@PathVariable String tenantId,
                                        @PathVariable String auctionId,
                                        @Valid @RequestBody RegisterBidderRequest request) {
        return onlineAuctionService.registerBidder(tenantId, auctionId, request);
    }

    @PostMapping("/{auctionId}/bids")
    OnlineAuctionResponse placeBid(@PathVariable String tenantId,
                                   @PathVariable String auctionId,
                                   @Valid @RequestBody PlaceOnlineBidRequest request) {
        return onlineAuctionService.placeBid(tenantId, auctionId, request);
    }

    @PostMapping("/{auctionId}/realtime-session")
    RealtimeSessionResponse issueRealtimeSession(@PathVariable String tenantId,
                                                 @PathVariable String auctionId,
                                                 @Valid @RequestBody RealtimeSessionRequest request) {
        return onlineAuctionService.issueRealtimeSession(tenantId, auctionId, request.accessToken());
    }
}
