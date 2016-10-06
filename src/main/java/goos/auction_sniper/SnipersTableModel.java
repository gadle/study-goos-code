package goos.auction_sniper;

import javax.swing.table.AbstractTableModel;

import static goos.auction_sniper.MainWindow.*;

public class SnipersTableModel extends AbstractTableModel {
    private final static SniperSnapshot STARTING_UP = new SniperSnapshot("", 0, 0, SniperState.BIDDING);
    private final static String[] STATUS_TEXT = {
        MainWindow.STATUS_JOINING,
        MainWindow.STATUS_BIDDING,
        MainWindow.STATUS_WINNING,
        MainWindow.STATUS_LOST,
        MainWindow.STATUS_WON
    };

    private String state = STATUS_JOINING;
    private SniperSnapshot snapshot = STARTING_UP;

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (Column.at(columnIndex)) {
            case ITEM_IDENTIFIER:
                return snapshot.itemId;
            case LAST_PRICE:
                return snapshot.lastPrice;
            case LAST_BID:
                return snapshot.lastBid;
            case SNIPER_STATUS:
                return state;
            default:
                throw new IllegalArgumentException("No column at " + columnIndex);
        }
    }

    public void sniperStatusChanged(SniperSnapshot snapshot) {
        this.snapshot = snapshot;
        this.state = STATUS_TEXT[snapshot.state.ordinal()];
        fireTableRowsUpdated(0, 0);
    }

    public void setState(String state) {
        this.state = state;
        fireTableRowsUpdated(0, 0);
    }

    public enum Column {
        ITEM_IDENTIFIER,
        LAST_PRICE,
        LAST_BID,
        SNIPER_STATUS;

        public static Column at(int offset) {
            return values()[offset];
        }
    }
}
