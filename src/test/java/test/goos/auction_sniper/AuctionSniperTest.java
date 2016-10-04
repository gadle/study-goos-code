package test.goos.auction_sniper;

import goos.auction_sniper.Auction;
import goos.auction_sniper.AuctionSniper;
import goos.auction_sniper.SniperListener;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AuctionSniperTest {
    private final SniperListener sniperListener = mock(SniperListener.class);
    private final Auction auction = mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);

    @Test public void reportsLostWhenAuctionCloses() {
        sniper.auctionClosed();

        verify(sniperListener).sniperLost();
    }

    @Test public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;

        sniper.currentPrice(price, increment);

        verify(auction).bid(price + increment);
        verify(sniperListener, atLeastOnce()).sniperBidding();
    }
}
