package goos.auction_sniper;

public interface AuctionEventListener {
    void auctionClosed();

    void currentPrice(int price, int increment);
}
