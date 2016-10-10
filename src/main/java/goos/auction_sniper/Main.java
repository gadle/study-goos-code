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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_FIRST_ITEM_ID = 3;

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private final SnipersTableModel snipers = new SnipersTableModel();
    private MainWindow ui;

    @SuppressWarnings("unused") private List<Chat> notToBeGCd = new ArrayList<>();

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();

        AbstractXMPPConnection connection = connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]);
        main.disconnectWhenUICloses(connection);
        for (int i = ARG_FIRST_ITEM_ID; i < args.length; i++) {
            main.joinAuction(connection, args[i]);
        }
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
    ) throws XMPPException, IOException, SmackException, InterruptedException, InvocationTargetException {
        safelyAddItemToModel(itemId);
        final Chat chat = ChatManager.getInstanceFor(connection).createChat(auctionId(itemId, connection));
        this.notToBeGCd.add(chat);

        Auction auction = new XMPPAuction(chat);

        chat.addMessageListener(new AuctionMessageTranslator(
                connection.getUser(),
                new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers))));
        auction.join();
    }

    private void safelyAddItemToModel(String itemId) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> snipers.addSniper(SniperSnapshot.joining(itemId)));
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
            ui = new MainWindow(snipers);
        });
    }

    public class SwingThreadSniperListener implements SniperListener {
        private final SniperListener listener;

        public SwingThreadSniperListener(SniperListener listener) {
            this.listener = listener;
        }

        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            SwingUtilities.invokeLater(() -> listener.sniperStateChanged(sniperSnapshot));
        }

    }
}