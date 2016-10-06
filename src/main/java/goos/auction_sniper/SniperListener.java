package goos.auction_sniper;

import java.util.EventListener;

public interface SniperListener extends EventListener {
    void sniperLost();

    void sniperWon();

    void sniperStateChanged(SniperSnapshot sniperSnapshot);
}
