package test.goos.auction_sniper;

import goos.auction_sniper.*;
import goos.auction_sniper.AuctionEventListener.PriceSource;
import org.hamcrest.FeatureMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AuctionSniperTest {
    private static final String ITEM_ID = "1234556789";
    private static final int STOP_PRICE = 1234;
    private static final Item ITEM = Item.create(ITEM_ID, STOP_PRICE);

    private final SniperListener sniperListener = spy(new SniperListenerStub());
    private final Auction auction = mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(ITEM, auction);

    private ObservedSniperState sniperState = ObservedSniperState.idle;

    @Before public void attachListener() {
        sniper.addSniperListener(sniperListener);
    }

    @Test public void reportsLostIfAuctionClosesImmediately() {
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 0, 0, SniperState.LOST));
    }

    @Test public void reportsLostIfAuctionClosesWhenBidding() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 123, 168, SniperState.LOST));
        assertEquals(ObservedSniperState.bidding, sniperState);
    }

    @Test public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);

        verify(auction).bid(bid);
        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
    }

    @Test public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WINNING));
    }

    @Test public void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WON));
        assertEquals(ObservedSniperState.winning, sniperState);
    }

    @Test public void doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        final int bid = 123 + 45;
        final SniperSnapshot losingSnapshot = new SniperSnapshot(ITEM_ID, 2345, bid, SniperState.LOSING);

        setState(ObservedSniperState.bidding)
                .when(sniperListener).sniperStateChanged(aSniperThatIs(SniperState.BIDDING));
        checkState(ObservedSniperState.bidding)
                .when(sniperListener).sniperStateChanged(losingSnapshot);

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder);

        verify(auction).bid(bid);
        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 2345, bid, SniperState.LOSING));
    }

    @Test public void doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
        final int bid = 123 + 45;
        final SniperSnapshot losingSnapshot = new SniperSnapshot(ITEM_ID, 1300, bid, SniperState.LOSING);

        setState(ObservedSniperState.bidding)
                .when(sniperListener).sniperStateChanged(aSniperThatIs(SniperState.BIDDING));
        checkState(ObservedSniperState.bidding)
                .when(sniperListener).sniperStateChanged(losingSnapshot);

        sniper.currentPrice(1300, 45, PriceSource.FromOtherBidder);

        verify(auction, never()).bid(anyInt());

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1300, 0, SniperState.LOSING));
    }

    @Test public void reportsLostIfAuctionClosesWhenLosing() {
        sniper.currentPrice(1300, 45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1300, 0, SniperState.LOST));
    }

    @Test public void continuesToBeLosingOnceStopPriceHasBeenReached() {
        sniper.currentPrice(1300, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(1350, 45, PriceSource.FromOtherBidder);

        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1300, 0, SniperState.LOSING));
        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1350, 0, SniperState.LOSING));
    }

    @Test public void doesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        final int bid = 123 + 45;

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(bid, 45, PriceSource.FromSniper);
        sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder);

        verify(auction).bid(bid);
        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 2345, bid, SniperState.LOSING));
    }

    //

    private SniperSnapshot aSniperThatIs(SniperState state) {
        return MockitoHamcrest.argThat(new FeatureMatcher<SniperSnapshot, SniperState>(
                equalTo(state), "state", "state") {
            @Override protected SniperState featureValueOf(SniperSnapshot actual) {
                return actual.state;
            }
        });
    }

    private Stubber checkState(ObservedSniperState expectedState) {
        return doAnswer(new StateCheckingAnswer(expectedState));
    }

    private Stubber setState(ObservedSniperState targetState) {
        return doAnswer(new StateSettingAnswer(targetState));
    }

    /**
     * We had to look for an alternative for jMock's "context.states" that has no direct
     * equivalent in Mockito. We implemented the solution described here:
     * http://stackoverflow.com/a/22047770/302264
     */
    private enum ObservedSniperState { idle, bidding, winning, losing }

    private class SniperListenerStub implements SniperListener {

        private ObservedSniperState state;

        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            if (SniperState.BIDDING == sniperSnapshot.state) {
                AuctionSniperTest.this.sniperState = state = ObservedSniperState.bidding;
            } else if (SniperState.WINNING == sniperSnapshot.state) {
                AuctionSniperTest.this.sniperState = state = ObservedSniperState.winning;
            } else if (SniperState.LOSING == sniperSnapshot.state) {
                AuctionSniperTest.this.sniperState = state = ObservedSniperState.losing;
            }
        }

        public ObservedSniperState observedState() {
            return state;
        }

        public void observedState(ObservedSniperState state) {
            this.state = state;
        }
    }

    private static class StateCheckingAnswer implements Answer<Void> {
        private final ObservedSniperState expectedState;

        public StateCheckingAnswer(ObservedSniperState expectedState) {
            this.expectedState = expectedState;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            final ObservedSniperState observedState = ((SniperListenerStub) invocation.getMock()).observedState();
            if (expectedState != observedState) {
                throw new IllegalStateException("expected sniper to be in state " + expectedState +
                    " but was in state " + observedState);
            }
            return null;
        }
    }

    private static class StateSettingAnswer implements Answer<Void> {
        private final ObservedSniperState targetState;

        public StateSettingAnswer(ObservedSniperState targetState) {
            this.targetState = targetState;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            ((SniperListenerStub) invocation.getMock()).observedState(targetState);
            return null;
        }
    }

}
