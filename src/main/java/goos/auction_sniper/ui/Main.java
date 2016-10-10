package goos.auction_sniper.ui;

import goos.auction_sniper.AuctionHouse;
import goos.auction_sniper.SniperLauncher;
import goos.auction_sniper.SniperPortfolio;
import goos.auction_sniper.xmpp.XMPPAuctionHouse;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;

    private final SniperPortfolio portfolio = new SniperPortfolio();
    private MainWindow ui;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();

        XMPPAuctionHouse auctionHouse = XMPPAuctionHouse.connect(
                args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]);
        main.disconnectWhenUICloses(auctionHouse);
        main.addUserRequestListenerFor(auctionHouse);
    }

    private void disconnectWhenUICloses(final AuctionHouse auctionHouse) {
        ui.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                auctionHouse.disconnect();
            }
        });
    }

    private void addUserRequestListenerFor(final AuctionHouse auctionHouse) {
        ui.addUserRequestListener(new SniperLauncher(auctionHouse, portfolio));
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ui = new MainWindow(portfolio);
        });
    }

}