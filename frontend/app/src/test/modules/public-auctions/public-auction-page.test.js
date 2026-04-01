import { flushPromises, mount } from "@vue/test-utils";
import PublicAuctionView from "../../../modules/public-auctions/ui/pages/public-auction-page";
import { setLocale } from "../../../app/i18n";
import router from "../../../app/router";
import * as onlineAuctionApi from "../../../modules/online-auctions/infrastructure/api/online-auction.api";

describe("PublicAuctionView", () => {
  it("loads the public auction detail", async () => {
    setLocale("en");
    vi.spyOn(onlineAuctionApi, "fetchPublicOnlineAuction").mockResolvedValue({
      id: "oa-1",
      title: "Live Gold Auction",
      minimumIncrement: 10,
      countdownEndsAt: "2026-03-18T22:30:00Z",
      lots: [{ id: "lot-1", lotNumber: 1, title: "Goldring", currentBid: 150, highestBidderAlias: "P1001" }],
      registrations: []
    });

    await router.push("/online-auctions/tenant-default/oa-1");
    await router.isReady();

    const wrapper = mount(PublicAuctionView, {
      global: { plugins: [router] }
    });
    await flushPromises();

    expect(wrapper.text()).toContain("Live Gold Auction");
    expect(wrapper.text()).toContain("P1001");
  });
});
