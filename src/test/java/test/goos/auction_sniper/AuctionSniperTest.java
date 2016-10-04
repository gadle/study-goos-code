package test.goos.auction_sniper;

import goos.auction_sniper.AuctionSniper;
import goos.auction_sniper.SniperListener;
import org.junit.Test;
import org.mockito.Mockito;

public class AuctionSniperTest {
    private final SniperListener sniperListener = Mockito.mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(sniperListener);

    @Test public void reportsLostWhenAuctionCloses() {
        sniper.auctionClosed();

        Mockito.verify(sniperListener).sniperLost();
    }
}
