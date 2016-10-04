package goos.auction_sniper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;

public class XMPPAuction implements Auction {
    private final Chat chat;

    public XMPPAuction(Chat chat) {
        this.chat = chat;
    }

    public void bid(int amount) {
        sendMessage(String.format(Main.BID_COMMAND_FORMAT, amount));
    }

    public void join() {
        sendMessage(Main.JOIN_COMMAND_FORMAT);
    }

    private void sendMessage(final String message) {
        try {
            chat.sendMessage(message);
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }
}
