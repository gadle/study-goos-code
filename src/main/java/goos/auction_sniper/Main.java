package goos.auction_sniper;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_ITEM_ID = 3;

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private MainWindow ui;

    @SuppressWarnings("unused") private Chat notToBeGCd;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();

        main.joinAuction(connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]), args[ARG_ITEM_ID]);
    }

    private void disconnectWhenUICloses(final AbstractXMPPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    private void joinAuction(
            AbstractXMPPConnection connection,
            String itemId
    ) throws XMPPException, IOException, SmackException {
        disconnectWhenUICloses(connection);

        final Chat chat = ChatManager.getInstanceFor(connection).createChat(auctionId(itemId, connection));
        this.notToBeGCd = chat;

        Auction auction = new XMPPAuction(chat);

        chat.addMessageListener(new AuctionMessageTranslator(new AuctionSniper(auction, new SniperStateDisplayer())));
        auction.join();
    }

    private static AbstractXMPPConnection connection(
            String hostname,
            String username,
            String password
    ) throws XMPPException, IOException, SmackException {
        AbstractXMPPConnection connection = new XMPPTCPConnection((XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setHost(hostname)
                .setServiceName(hostname).build()));

        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return connection;
    }

    private static String auctionId(String itemId, AbstractXMPPConnection connection) {
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ui = new MainWindow();
        });
    }

    public class SniperStateDisplayer implements SniperListener {

        @Override
        public void sniperLost() {
            showStatus(MainWindow.STATUS_LOST);
        }

        @Override
        public void sniperBidding() {
            showStatus(MainWindow.STATUS_BIDDING);
        }

        private void showStatus(final String status) {
            SwingUtilities.invokeLater(() -> ui.showStatus(status));
        }
    }
}