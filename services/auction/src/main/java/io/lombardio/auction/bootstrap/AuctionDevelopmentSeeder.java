package io.lombardio.auction.bootstrap;

import io.lombardio.auction.demo.DemoDataProperties;
import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.model.AuctionLot;
import io.lombardio.auction.domain.model.AuctionLotStatus;
import io.lombardio.auction.domain.model.AuctionStatus;
import io.lombardio.auction.domain.port.AuctionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuctionDevelopmentSeeder {

    private record DemoTenant(String id, String key, String numberPrefix, String city) {
    }

    private static final List<DemoTenant> TENANTS = List.of(
            new DemoTenant("tenant-default", "default", "BER", "Berlin"),
            new DemoTenant("tenant-hamburg", "hanseatic", "HAM", "Hamburg"),
            new DemoTenant("tenant-munich", "isar", "MUC", "Muenchen"),
            new DemoTenant("tenant-cologne", "rhein", "CGN", "Koeln"),
            new DemoTenant("tenant-stuttgart", "neckar", "STR", "Stuttgart")
    );

    private final AuctionRepository auctionRepository;
    private final DemoDataProperties demoDataProperties;

    AuctionDevelopmentSeeder(AuctionRepository auctionRepository, DemoDataProperties demoDataProperties) {
        this.auctionRepository = auctionRepository;
        this.demoDataProperties = demoDataProperties;
    }

    public void seed() {
        int tenantCount = tenantCount(demoDataProperties.effectiveScale());
        int auctionsPerTenant = auctionsPerTenant(demoDataProperties.effectiveScale());
        for (int tenantIndex = 0; tenantIndex < tenantCount; tenantIndex++) {
            DemoTenant tenant = TENANTS.get(tenantIndex);
            for (int auctionIndex = 1; auctionIndex <= auctionsPerTenant; auctionIndex++) {
                auctionRepository.save(buildAuction(tenant, tenantIndex, auctionIndex));
            }
        }
    }

    private Auction buildAuction(DemoTenant tenant, int tenantIndex, int auctionIndex) {
        AuctionStatus status = switch (auctionIndex % 5) {
            case 0 -> AuctionStatus.SETTLED;
            case 1 -> AuctionStatus.ANNOUNCED;
            case 2 -> AuctionStatus.LIVE;
            case 3 -> AuctionStatus.CLOSED;
            default -> AuctionStatus.DRAFT;
        };

        LocalDate announcementDate = LocalDate.now().minusDays(30L + auctionIndex * 3L);
        LocalDate auctionDate = announcementDate.plusDays(21);
        Instant liveStartedAt = status == AuctionStatus.LIVE || status == AuctionStatus.CLOSED || status == AuctionStatus.SETTLED
                ? auctionDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).plusSeconds(32_400)
                : null;
        Instant closedAt = status == AuctionStatus.CLOSED || status == AuctionStatus.SETTLED
                ? liveStartedAt.plusSeconds(18_000)
                : null;

        return new Auction(
                "auction-" + tenant.key() + "-" + String.format("%03d", auctionIndex),
                tenant.id(),
                tenant.city() + " Verwertung " + String.format("%02d", auctionIndex),
                tenant.city() + " Saal " + ((auctionIndex % 3) + 1),
                status,
                status == AuctionStatus.DRAFT ? null : announcementDate,
                status == AuctionStatus.DRAFT ? null : auctionDate,
                liveStartedAt,
                closedAt,
                "AUK-" + tenant.numberPrefix() + "-" + String.format("%05d", auctionIndex),
                buildLots(tenant, auctionIndex, status),
                Instant.now().minusSeconds((long) (auctionIndex + tenantIndex * 14) * 86_400L),
                Instant.now().minusSeconds((long) (auctionIndex + tenantIndex * 14 - 1) * 86_400L)
        );
    }

    private List<AuctionLot> buildLots(DemoTenant tenant, int auctionIndex, AuctionStatus auctionStatus) {
        int lotCount = 6 + (auctionIndex % 4);
        List<AuctionLot> lots = new ArrayList<>();
        for (int lotIndex = 1; lotIndex <= lotCount; lotIndex++) {
            BigDecimal estimatedValue = new BigDecimal(120 + (lotIndex * 45L) + (auctionIndex * 10L) + ".00");
            BigDecimal outstandingClaim = estimatedValue.multiply(new BigDecimal("0.65")).setScale(2, java.math.RoundingMode.HALF_UP);
            AuctionLotStatus lotStatus = switch (auctionStatus) {
                case DRAFT, ANNOUNCED -> AuctionLotStatus.PENDING;
                case LIVE -> lotIndex % 3 == 0 ? AuctionLotStatus.SOLD : AuctionLotStatus.OPEN;
                case CLOSED, SETTLED -> lotIndex % 4 == 0 ? AuctionLotStatus.UNSOLD : AuctionLotStatus.SOLD;
            };
            BigDecimal latestBidAmount = lotStatus == AuctionLotStatus.PENDING ? BigDecimal.ZERO : outstandingClaim.add(new BigDecimal(20 + lotIndex * 5L + ".00"));
            BigDecimal hammerPrice = lotStatus == AuctionLotStatus.SOLD ? latestBidAmount.add(new BigDecimal("15.00")) : null;
            BigDecimal surplusAmount = hammerPrice == null ? null : hammerPrice.subtract(outstandingClaim).max(BigDecimal.ZERO);

            lots.add(new AuctionLot(
                    "auction-lot-" + tenant.key() + "-" + String.format("%03d", auctionIndex) + "-" + String.format("%02d", lotIndex),
                    "auction-" + tenant.key() + "-" + String.format("%03d", auctionIndex),
                    lotIndex,
                    "VT-" + tenant.numberPrefix() + "-" + String.format("%05d", auctionIndex * 10 + lotIndex),
                    "ITEM-" + tenant.numberPrefix() + "-" + String.format("%05d", auctionIndex * 10 + lotIndex),
                    lotIndex % 2 == 0 ? "Goldschmuck Konvolut" : "Elektronik Paket " + lotIndex,
                    estimatedValue,
                    outstandingClaim,
                    latestBidAmount,
                    lotStatus == AuctionLotStatus.SOLD || lotStatus == AuctionLotStatus.OPEN ? "Bieter " + lotIndex : null,
                    hammerPrice,
                    lotStatus,
                    surplusAmount,
                    lotStatus == AuctionLotStatus.SOLD ? LocalDate.now().plusDays(14 + lotIndex) : null,
                    lotStatus == AuctionLotStatus.SOLD ? "PENDING_TRANSFER" : null
            ));
        }
        return lots;
    }

    private int tenantCount(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 2;
            case "large" -> TENANTS.size();
            default -> 4;
        };
    }

    private int auctionsPerTenant(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 2;
            case "large" -> 7;
            default -> 4;
        };
    }

    private String normalize(String scale) {
        return scale == null ? "medium" : scale.trim().toLowerCase();
    }
}
