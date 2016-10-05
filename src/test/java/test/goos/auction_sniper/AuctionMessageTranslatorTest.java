package test.goos.auction_sniper;

import goos.auction_sniper.AuctionEventListener;
import goos.auction_sniper.AuctionEventListener.PriceSource;
import goos.auction_sniper.AuctionMessageTranslator;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.junit.Test;
import org.mockito.Mockito;

import static test.goos.auction_sniper.ApplicationRunner.SNIPER_ID;

public class AuctionMessageTranslatorTest {
    public static final Chat UNUSED_CHAT = null;
    private final AuctionEventListener listener = Mockito.mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_ID, listener);

    @Test public void notifiesAuctionClosedWhenCloseMessageReceived() {
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: CLOSE;");

        translator.processMessage(UNUSED_CHAT, message);

        Mockito.verify(listener).auctionClosed();
    }

    @Test public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;");

        translator.processMessage(UNUSED_CHAT, message);

        Mockito.verify(listener).currentPrice(192, 7, PriceSource.FromOtherBidder);
    }


    @Test public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";");

        translator.processMessage(UNUSED_CHAT, message);

        Mockito.verify(listener).currentPrice(234, 5, PriceSource.FromSniper);
    }
}
