package test.goos.auction_sniper;

import goos.auction_sniper.Defect;
import goos.auction_sniper.SniperSnapshot;
import goos.auction_sniper.SnipersTableModel;
import goos.auction_sniper.SnipersTableModel.Column;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.hamcrest.MockitoHamcrest;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static goos.auction_sniper.SnipersTableModel.textFor;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
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
        SniperSnapshot joining = SniperSnapshot.joining("item id");
        SniperSnapshot bidding = joining.bidding(555, 666);

        model.addSniperSnapshot(joining);
        model.sniperStateChanged(bidding);

        assertRowMatchesSnapshot(0, bidding);
        verify(listener).tableChanged(MockitoHamcrest.argThat(isChangeInRow(0)));
    }

    @Test public void setsUpColumnHeadings() {
        for (Column column: Column.values()) {
            assertEquals(column.name, model.getColumnName(column.ordinal()));
        }
    }

    @Test public void notifiesListenersWhenAddingASniper() {
        SniperSnapshot joining = SniperSnapshot.joining("item123");

        assertEquals(0, model.getRowCount());

        model.addSniperSnapshot(joining);

        assertEquals(1, model.getRowCount());
        assertRowMatchesSnapshot(0, joining);

        verify(listener).tableChanged(MockitoHamcrest.argThat(isInsertionAtRow(0)));
    }

    @Test public void holdsSnipersInAdditionOrder() {
        model.addSniperSnapshot(SniperSnapshot.joining("item 0"));
        model.addSniperSnapshot(SniperSnapshot.joining("item 1"));

        assertEquals("item 0", cellValue(0, Column.ITEM_IDENTIFIER));
        assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER));
    }

    @Test public void updatesCorrectRowForSniper() {
        SniperSnapshot snapshot0 = SniperSnapshot.joining("item 0");
        SniperSnapshot snapshot1 = SniperSnapshot.joining("item 1");

        model.addSniperSnapshot(snapshot0);
        model.addSniperSnapshot(snapshot1);

        snapshot0 = snapshot0.bidding(555, 666);
        model.sniperStateChanged(snapshot0);

        assertRowMatchesSnapshot(0, snapshot0);
        assertRowMatchesSnapshot(1, snapshot1);
    }

    @Test(expected = Defect.class)
    public void throwsDefectIfNoExistingSniperForAnUpdate() {
        SniperSnapshot snapshot0 = SniperSnapshot.joining("item 0");
        SniperSnapshot snapshot1 = SniperSnapshot.joining("item 1");

        model.addSniperSnapshot(snapshot0);

        snapshot1 = snapshot1.bidding(555, 666);
        model.sniperStateChanged(snapshot1);
    }

    private Matcher<TableModelEvent> isChangeInRow(int rowIndex) {
        return allOf(
                hasProperty("type", equalTo(TableModelEvent.UPDATE)),
                hasProperty("firstRow", equalTo(rowIndex)),
                hasProperty("lastRow", equalTo(rowIndex)));
    }

    private Matcher<TableModelEvent> isInsertionAtRow(int rowIndex) {
        return allOf(
                hasProperty("type", equalTo(TableModelEvent.INSERT)),
                hasProperty("firstRow", equalTo(rowIndex)),
                hasProperty("lastRow", equalTo(rowIndex)));
    }

    private Object cellValue(int rowIndex, Column column) {
        final int columnIndex = column.ordinal();
        return model.getValueAt(rowIndex, columnIndex);
    }

    private void assertCellEquals(int rowIndex, Column column, Object expected) {
        assertEquals(expected, cellValue(rowIndex, column));
    }

    private void assertRowMatchesSnapshot(int rowIndex, SniperSnapshot snapshot) {
        assertCellEquals(rowIndex, Column.ITEM_IDENTIFIER, snapshot.itemId);
        assertCellEquals(rowIndex, Column.LAST_PRICE, snapshot.lastPrice);
        assertCellEquals(rowIndex, Column.LAST_BID, snapshot.lastBid);
        assertCellEquals(rowIndex, Column.SNIPER_STATUS, textFor(snapshot.state));
    }
}