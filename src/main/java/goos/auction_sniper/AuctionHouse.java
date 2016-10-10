package goos.auction_sniper;

public interface AuctionHouse {
    Auction auctionFor(String itemId);

    void disconnect();
}
