package io.lombardio.auction.infrastructure.persistence.entity;

import io.lombardio.auction.domain.model.AuctionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auctions", schema = "auction")
public class AuctionEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status;

    private LocalDate publicAnnouncementDate;

    private LocalDate auctionDate;

    private Instant liveStartedAt;

    private Instant closedAt;

    private String announcementReference;

    @Column(nullable = false)
    private String realtimeChannel;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lotNumber asc")
    private List<AuctionLotEntity> lots = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public LocalDate getPublicAnnouncementDate() { return publicAnnouncementDate; }
    public void setPublicAnnouncementDate(LocalDate publicAnnouncementDate) { this.publicAnnouncementDate = publicAnnouncementDate; }
    public LocalDate getAuctionDate() { return auctionDate; }
    public void setAuctionDate(LocalDate auctionDate) { this.auctionDate = auctionDate; }
    public Instant getLiveStartedAt() { return liveStartedAt; }
    public void setLiveStartedAt(Instant liveStartedAt) { this.liveStartedAt = liveStartedAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public String getAnnouncementReference() { return announcementReference; }
    public void setAnnouncementReference(String announcementReference) { this.announcementReference = announcementReference; }
    public String getRealtimeChannel() { return realtimeChannel; }
    public void setRealtimeChannel(String realtimeChannel) { this.realtimeChannel = realtimeChannel; }
    public List<AuctionLotEntity> getLots() { return lots; }
    public void setLots(List<AuctionLotEntity> lots) { this.lots = lots; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
