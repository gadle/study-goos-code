package goos.auction_sniper.xmpp;

import goos.auction_sniper.Auction;
import goos.auction_sniper.AuctionHouse;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

public class XMPPAuctionHouse implements AuctionHouse {

    private static final String AUCTION_RESOURCE = "Auction";
    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    private final AbstractXMPPConnection connection;

    private XMPPAuctionHouse(AbstractXMPPConnection connection) {
        this.connection = connection;
    }

    @Override
    public Auction auctionFor(String itemId) {
        return new XMPPAuction(connection, auctionJID(itemId, connection));
    }

    public static XMPPAuctionHouse connect(
            String hostname, String username, String password)
            throws IOException, XMPPException, SmackException
    {
        AbstractXMPPConnection connection = new XMPPTCPConnection((XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setHost(hostname)
                .setServiceName(hostname).build()));

        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return new XMPPAuctionHouse(connection);
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    private static String auctionJID(String itemId, AbstractXMPPConnection connection) {
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
    }

}
