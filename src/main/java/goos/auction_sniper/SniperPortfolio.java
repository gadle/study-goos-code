package goos.auction_sniper;

import goos.auction_sniper.util.Announcer;

import java.util.ArrayList;
import java.util.List;

public class SniperPortfolio implements SniperCollector {
    private final List<AuctionSniper> snipers = new ArrayList<>();
    private final Announcer<PortfolioListener> portfolioListeners = Announcer.to(PortfolioListener.class);

    public void addPortfolioListener(PortfolioListener listener) {
        portfolioListeners.addListener(listener);
    }

    @Override
    public void addSniper(AuctionSniper sniper) {
        snipers.add(sniper);
        portfolioListeners.announce().sniperAdded(sniper);
    }
}
