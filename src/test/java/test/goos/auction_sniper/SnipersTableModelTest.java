package test.goos.auction_sniper;

import goos.auction_sniper.MainWindow;
import goos.auction_sniper.SniperSnapshot;
import goos.auction_sniper.SniperState;
import goos.auction_sniper.SnipersTableModel;
import goos.auction_sniper.SnipersTableModel.Column;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.hamcrest.MockitoHamcrest;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class SnipersTableModelTest {
    private TableModelListener listener = mock(TableModelListener.class);
    private final SnipersTableModel model = new SnipersTableModel();

    @Before public void attachModelListener() {
        model.addTableModelListener(listener);
    }

    @Test public void hasEnoughColumns() {
        assertThat(model.getColumnCount(), equalTo(Column.values().length));
    }

    @Test public void setsSniperValuesInColumns() {
        model.sniperStatusChanged(new SniperSnapshot("item id", 555, 666, SniperState.BIDDING));

        verify(listener).tableChanged(MockitoHamcrest.argThat(aRowChangedEvent()));
        assertColumnEquals(Column.ITEM_IDENTIFIER, "item id");
        assertColumnEquals(Column.LAST_PRICE, 555);
        assertColumnEquals(Column.LAST_BID, 666);
        assertColumnEquals(Column.SNIPER_STATUS, MainWindow.STATUS_BIDDING);
    }

    private void assertColumnEquals(Column column, Object expected) {
        final int rowIndex = 0;
        final int columnIndex = column.ordinal();
        assertEquals(expected, model.getValueAt(rowIndex, columnIndex));
    }

    private Matcher<TableModelEvent> aRowChangedEvent() {
        return samePropertyValuesAs(new TableModelEvent(model, 0 /* rowIndex */));
    }
}