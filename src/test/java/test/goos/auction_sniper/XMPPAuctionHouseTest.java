package test.goos.auction_sniper;

import goos.auction_sniper.Auction;
import goos.auction_sniper.AuctionEventListener;
import goos.auction_sniper.AuctionHouse;
import goos.auction_sniper.Item;
import goos.auction_sniper.xmpp.XMPPAuctionHouse;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

public class XMPPAuctionHouseTest {

    private final FakeAuctionServer auctionServer = new FakeAuctionServer("item-54321");
    private AuctionHouse auctionHouse;

    @Before public void openConnection() throws Exception {
        auctionHouse = XMPPAuctionHouse.connect(FakeAuctionServer.XMPP_HOSTNAME,
                ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD);
    }

    @Before public void startAuction() throws Exception {
        auctionServer.startSellingItem();
    }

    @Test public void receivesEventsFromAuctionServerAfterJoining() throws Exception {
        CountDownLatch auctionWasClosed = new CountDownLatch(1);
        Item item = Item.create(auctionServer.getItemId(), Integer.MAX_VALUE);

        Auction auction = auctionHouse.auctionFor(item);
        auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed));

        auction.join();
        auctionServer.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
        auctionServer.announceClosed();

        assertTrue("should have been closed", auctionWasClosed.await(2, SECONDS));
    }

    private AuctionEventListener auctionClosedListener(final CountDownLatch auctionWasClosed) {
        return new AuctionEventListener() {
            public void auctionClosed() {
                auctionWasClosed.countDown();
            }

            public void currentPrice(int price, int increment, PriceSource fromOtherBidder) {
            }
        };
    }

}