package goos.auction_sniper;

public interface AuctionHouse {
    Auction auctionFor(Item item);

    void disconnect();
}
