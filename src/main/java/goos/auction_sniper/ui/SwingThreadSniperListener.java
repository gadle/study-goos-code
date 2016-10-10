package goos.auction_sniper.ui;

import goos.auction_sniper.SniperListener;
import goos.auction_sniper.SniperSnapshot;

import javax.swing.*;

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
