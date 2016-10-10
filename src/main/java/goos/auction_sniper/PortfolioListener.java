package goos.auction_sniper;

import java.util.EventListener;

public interface PortfolioListener extends EventListener {
    void sniperAdded(AuctionSniper sniper);
}
