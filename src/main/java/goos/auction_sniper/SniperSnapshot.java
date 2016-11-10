package goos.auction_sniper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SniperSnapshot {
    public final String itemId;
    public final int lastPrice;
    public final int lastBid;
    public final SniperState state;

    public SniperSnapshot(String itemId, int lastPrice, int lastBid, SniperState state) {
        this.itemId = itemId;
        this.lastPrice = lastPrice;
        this.lastBid = lastBid;
        this.state = state;
    }

    public static SniperSnapshot joining(String itemId) {
        return new SniperSnapshot(itemId, 0, 0, SniperState.JOINING);
    }

    public SniperSnapshot winning(int lastPrice) {
        return new SniperSnapshot(itemId, lastPrice, lastBid, SniperState.WINNING);
    }

    public SniperSnapshot losing(int lastPrice) {
        return new SniperSnapshot(itemId, lastPrice, lastBid, SniperState.LOSING);
    }

    public SniperSnapshot bidding(int lastPrice, int lastBid) {
        return new SniperSnapshot(itemId, lastPrice, lastBid, SniperState.BIDDING);
    }

    public SniperSnapshot closed() {
        return new SniperSnapshot(itemId, lastPrice, lastBid, state.whenAuctionClosed());
    }

    public boolean isForSameItemAs(SniperSnapshot that) {
        return this.itemId.equals(that.itemId);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
