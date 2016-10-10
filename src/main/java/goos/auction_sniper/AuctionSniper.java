package goos.auction_sniper;

import goos.auction_sniper.util.Announcer;

public class AuctionSniper implements AuctionEventListener {
    private final Auction auction;
    private final Announcer<SniperListener> sniperListeners = Announcer.to(SniperListener.class);
    private SniperSnapshot snapshot;

    public AuctionSniper(String itemId, Auction auction) {
        this.auction = auction;
        this.snapshot = SniperSnapshot.joining(itemId);
        notifyChange();
    }

    public void addSniperListener(SniperListener listener) {
        sniperListeners.addListener(listener);
    }

    public SniperSnapshot snapshot() {
        return snapshot;
    }

    @Override
    public void auctionClosed() {
        snapshot = snapshot.closed();
        notifyChange();
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        switch (priceSource) {
            case FromSniper:
                snapshot = snapshot.winning(price);
                break;
            case FromOtherBidder:
                int bid = price + increment;
                auction.bid(bid);
                snapshot = snapshot.bidding(price, bid);
                break;
        }
        notifyChange();
    }

    private void notifyChange() {
        sniperListeners.announce().sniperStateChanged(snapshot);
    }
}
