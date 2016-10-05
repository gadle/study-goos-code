package test.goos.auction_sniper;

import goos.auction_sniper.Auction;
import goos.auction_sniper.AuctionEventListener.PriceSource;
import goos.auction_sniper.AuctionSniper;
import goos.auction_sniper.SniperListener;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AuctionSniperTest {
    private final SniperListener sniperListener = spy(new SniperListenerStub());
    private final Auction auction = mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);

    private SniperState sniperState = SniperState.idle;

    @Test public void reportsLostIfAuctionClosesImmediately() {
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperLost();
    }

    @Test public void reportsLostIfAuctionClosesWhenBidding() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperLost();
        assertEquals(SniperState.bidding, sniperState);
    }

    @Test public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);

        verify(auction).bid(price + increment);
        verify(sniperListener, atLeastOnce()).sniperBidding();
    }

    @Test public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 45, PriceSource.FromSniper);

        verify(sniperListener, atLeastOnce()).sniperWinning();
    }

    @Test public void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123,45, PriceSource.FromSniper);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperWon();
        assertEquals(SniperState.winning, sniperState);
    }

    /**
     * We had to look for an alternative for jMock's "context.states" that has no direct
     * equivalent in Mockito. We implemented the solution described here:
     * http://stackoverflow.com/a/22047770/302264
     */
    private enum SniperState { idle, bidding, winning }

    private class SniperListenerStub implements SniperListener {
        public void sniperLost() {}

        @Override
        public void sniperBidding() {
            sniperState = SniperState.bidding;
        }

        @Override
        public void sniperWinning() {
            sniperState = SniperState.winning;
        }

        @Override
        public void sniperWon() {
        }
    }
}
