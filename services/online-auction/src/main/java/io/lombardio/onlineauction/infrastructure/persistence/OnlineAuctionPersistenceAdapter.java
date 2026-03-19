package io.lombardio.onlineauction.infrastructure.persistence;

import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionLot;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class OnlineAuctionPersistenceAdapter implements OnlineAuctionRepository {

    private final SpringDataOnlineAuctionRepository repository;

    public OnlineAuctionPersistenceAdapter(SpringDataOnlineAuctionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineAuction> findByTenantId(String tenantId) {
        return repository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineAuction> findPublicByTenantId(String tenantId) {
        return repository.findByTenantIdAndStatusInOrderByPublishedAtDesc(
                tenantId, List.of(OnlineAuctionStatus.PUBLISHED, OnlineAuctionStatus.LIVE, OnlineAuctionStatus.CLOSED)
        ).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OnlineAuction> findByTenantIdAndId(String tenantId, String auctionId) {
        return repository.findByTenantIdAndId(tenantId, auctionId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OnlineAuction> findPublicByTenantIdAndId(String tenantId, String auctionId) {
        return repository.findByTenantIdAndIdAndStatusIn(
                tenantId, auctionId, List.of(OnlineAuctionStatus.PUBLISHED, OnlineAuctionStatus.LIVE, OnlineAuctionStatus.CLOSED)
        ).map(this::toDomain);
    }

    @Override
    public OnlineAuction save(OnlineAuction auction) {
        OnlineAuctionEntity entity = new OnlineAuctionEntity();
        entity.setId(auction.id());
        entity.setTenantId(auction.tenantId());
        entity.setTitle(auction.title());
        entity.setSlug(auction.slug());
        entity.setStatus(auction.status());
        entity.setChannelName(auction.channelName());
        entity.setMinimumIncrement(auction.minimumIncrement());
        entity.setCountdownSeconds(auction.countdownSeconds());
        entity.setPublishedAt(auction.publishedAt());
        entity.setLiveStartedAt(auction.liveStartedAt());
        entity.setCountdownEndsAt(auction.countdownEndsAt());
        entity.setClosedAt(auction.closedAt());
        entity.setCreatedAt(auction.createdAt());
        entity.setUpdatedAt(auction.updatedAt());
        entity.setLots(auction.lots().stream().map(item -> toLotEntity(item, entity)).toList());
        entity.setRegistrations(auction.registrations().stream().map(item -> toRegistrationEntity(item, entity)).toList());
        return toDomain(repository.save(entity));
    }

    private OnlineAuctionLotEntity toLotEntity(OnlineAuctionLot lot, OnlineAuctionEntity auction) {
        OnlineAuctionLotEntity entity = new OnlineAuctionLotEntity();
        entity.setId(lot.id());
        entity.setAuction(auction);
        entity.setLotNumber(lot.lotNumber());
        entity.setTitle(lot.title());
        entity.setDescription(lot.description());
        entity.setStartingBid(lot.startingBid());
        entity.setCurrentBid(lot.currentBid());
        entity.setHighestBidderAlias(lot.highestBidderAlias());
        return entity;
    }

    private BidderRegistrationEntity toRegistrationEntity(BidderRegistration registration, OnlineAuctionEntity auction) {
        BidderRegistrationEntity entity = new BidderRegistrationEntity();
        entity.setId(registration.id());
        entity.setAuction(auction);
        entity.setDisplayName(registration.displayName());
        entity.setEmail(registration.email());
        entity.setLegalName(registration.legalName());
        entity.setBirthDate(registration.birthDate());
        entity.setIban(registration.iban());
        entity.setPaddleNumber(registration.paddleNumber());
        entity.setAccessToken(registration.accessToken());
        entity.setApprovalStatus(registration.approvalStatus());
        entity.setKycStatus(registration.kycStatus());
        entity.setAccountCheckStatus(registration.accountCheckStatus());
        entity.setReviewNote(registration.reviewNote());
        entity.setApprovedAt(registration.approvedAt());
        entity.setCreatedAt(registration.createdAt());
        return entity;
    }

    private OnlineAuction toDomain(OnlineAuctionEntity entity) {
        return new OnlineAuction(
                entity.getId(),
                entity.getTenantId(),
                entity.getTitle(),
                entity.getSlug(),
                entity.getStatus(),
                entity.getChannelName(),
                entity.getMinimumIncrement(),
                entity.getCountdownSeconds(),
                entity.getPublishedAt(),
                entity.getLiveStartedAt(),
                entity.getCountdownEndsAt(),
                entity.getClosedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLots().stream().map(this::toLot).toList(),
                entity.getRegistrations().stream().map(this::toRegistration).toList()
        );
    }

    private OnlineAuctionLot toLot(OnlineAuctionLotEntity entity) {
        return new OnlineAuctionLot(
                entity.getId(),
                entity.getLotNumber(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStartingBid(),
                entity.getCurrentBid(),
                entity.getHighestBidderAlias()
        );
    }

    private BidderRegistration toRegistration(BidderRegistrationEntity entity) {
        return new BidderRegistration(
                entity.getId(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getLegalName(),
                entity.getBirthDate(),
                entity.getIban(),
                entity.getPaddleNumber(),
                entity.getAccessToken(),
                entity.getApprovalStatus(),
                entity.getKycStatus(),
                entity.getAccountCheckStatus(),
                entity.getReviewNote(),
                entity.getApprovedAt(),
                entity.getCreatedAt()
        );
    }
}
