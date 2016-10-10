package goos.auction_sniper.xmpp;

import goos.auction_sniper.Auction;
import goos.auction_sniper.AuctionEventListener;
import goos.auction_sniper.util.Announcer;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;

public class XMPPAuction implements Auction {
    private final Announcer<AuctionEventListener> auctionEventListeners = Announcer.to(AuctionEventListener.class);
    private final Chat chat;

    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    public XMPPAuction(AbstractXMPPConnection connection, String auctionJID) {
        this.chat = ChatManager.getInstanceFor(connection).createChat(
                auctionJID,
                new AuctionMessageTranslator(connection.getUser(), auctionEventListeners.announce()));
    }

    @Override
    public void addAuctionEventListener(AuctionEventListener listener) {
        auctionEventListeners.addListener(listener);
    }

    public void bid(int amount) {
        sendMessage(String.format(BID_COMMAND_FORMAT, amount));
    }

    public void join() {
        sendMessage(JOIN_COMMAND_FORMAT);
    }

    private void sendMessage(final String message) {
        try {
            chat.sendMessage(message);
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }

}
