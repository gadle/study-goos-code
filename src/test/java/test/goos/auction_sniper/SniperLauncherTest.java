package test.goos.auction_sniper;

import goos.auction_sniper.*;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.hamcrest.MockitoHamcrest;
import test.goos.auction_sniper.util.StatesChecker;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class SniperLauncherTest {
    private final Auction auction = mock(Auction.class);
    private final SniperCollector sniperCollector = mock(SniperCollector.class);
    private final AuctionHouse auctionHouse = mock(AuctionHouse.class);
    private final SniperLauncher launcher =  new SniperLauncher(auctionHouse, sniperCollector);

    private enum ObservedAuctionState { not_joined, joined }
    private final StatesChecker<ObservedAuctionState> states = new StatesChecker<>(ObservedAuctionState.not_joined);

    @Test public void addsNewSniperToCollectorAndThenJoinsAuction() {
        final String itemId = "item 123";

        when(auctionHouse.auctionFor(itemId)).thenReturn(auction);

        doAnswer(states.verifyState(ObservedAuctionState.not_joined))
                .when(auction).addAuctionEventListener(MockitoHamcrest.argThat(sniperForItem(itemId)));
        doAnswer(states.verifyState(ObservedAuctionState.not_joined))
                .when(sniperCollector).addSniper(MockitoHamcrest.argThat(sniperForItem(itemId)));
        doAnswer(states.setState(ObservedAuctionState.joined)).when(auction).join();

        launcher.joinAuction(itemId);

        verify(auction).addAuctionEventListener(MockitoHamcrest.argThat(sniperForItem(itemId)));
        verify(sniperCollector).addSniper(MockitoHamcrest.argThat(sniperForItem(itemId)));
        verify(auction).join();
    }

    private Matcher<AuctionSniper> sniperForItem(String itemId) {
        return new FeatureMatcher<AuctionSniper, String>(equalTo(itemId), "sniper with item id", "item") {
            @Override protected String featureValueOf(AuctionSniper actual) {
                return actual.snapshot().itemId;
            }
        };
    }

}