package test.goos.auction_sniper.util;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Provide helper methods for checking observable states with Mockito. Similar to jMock "states".
 */
public class StatesChecker<T extends Enum> {
    private T state;

    public StatesChecker(T initialState) {
        this.state = initialState;
    }

    public Answer<Void> verifyState(final T desiredState) {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (state != desiredState) {
                    throw new IllegalStateException("state should have been " + desiredState);
                }
                return null;
            }
        };
    }

    public Answer<Void> setState(final T newState) {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                state = newState;
                return null;
            }
        };
    }
}
