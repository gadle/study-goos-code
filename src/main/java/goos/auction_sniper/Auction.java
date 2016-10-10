package goos.auction_sniper;

public interface Auction {
    void addAuctionEventListener(AuctionEventListener listener);

    void bid(int amount);

    void join();
}
