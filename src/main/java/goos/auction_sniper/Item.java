package goos.auction_sniper;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Item {
    public static Item create(String identifier, int stopPrice) {
        return new AutoValue_Item(identifier, stopPrice);
    }

    public abstract String identifier();
    public abstract int stopPrice();

    public boolean allowsBid(int bid) {
        return bid <= stopPrice();
    }
}
