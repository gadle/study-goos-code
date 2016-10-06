package test.goos.auction_sniper;

import goos.auction_sniper.*;
import goos.auction_sniper.AuctionEventListener.PriceSource;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AuctionSniperTest {
    private static final String ITEM_ID = "1234556789";
    private final SniperListener sniperListener = spy(new SniperListenerStub());
    private final Auction auction = mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction, sniperListener);

    private ObservedSniperState sniperState = ObservedSniperState.idle;

    @Test public void reportsLostIfAuctionClosesImmediately() {
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 0, 0, SniperState.LOST));
    }

    @Test public void reportsLostIfAuctionClosesWhenBidding() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 123, 168, SniperState.LOST));
        assertEquals(ObservedSniperState.bidding, sniperState);
    }

    @Test public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);

        verify(auction).bid(bid);
        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
    }

    @Test public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WINNING));
    }

    @Test public void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WON));
        assertEquals(ObservedSniperState.winning, sniperState);
    }

    /**
     * We had to look for an alternative for jMock's "context.states" that has no direct
     * equivalent in Mockito. We implemented the solution described here:
     * http://stackoverflow.com/a/22047770/302264
     */
    private enum ObservedSniperState { idle, bidding, winning }

    private class SniperListenerStub implements SniperListener {

        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            if (SniperState.BIDDING == sniperSnapshot.state) {
                AuctionSniperTest.this.sniperState = ObservedSniperState.bidding;
            } else if (SniperState.WINNING == sniperSnapshot.state) {
                AuctionSniperTest.this.sniperState = ObservedSniperState.winning;
            }
        }
    }
}
