package goos.auction_sniper;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SnipersTableModel extends AbstractTableModel implements
        SniperListener {
    private final static String[] STATUS_TEXT = {
        "Joining",
        "Bidding",
        "Winning",
        "Lost",
        "Won"
    };

    private List<SniperSnapshot> snapshots = new ArrayList<>();

    @Override
    public int getRowCount() {
        return snapshots.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return Column.at(columnIndex).valueIn(snapshots.get(rowIndex));
    }

    @Override
    public String getColumnName(int column) {
        return Column.at(column).name;
    }

    @Override
    public void sniperStateChanged(SniperSnapshot snapshot) {
        int row = rowMatching(snapshot);
        snapshots.set(row, snapshot);
        fireTableRowsUpdated(row, row);
    }

    public static String textFor(SniperState state) {
        return STATUS_TEXT[state.ordinal()];
    }

    public void addSniper(SniperSnapshot snapshot) {
        snapshots.add(snapshot);
        int newRow = snapshots.size() - 1;
        fireTableRowsInserted(newRow, newRow);
    }

    private int rowMatching(SniperSnapshot snapshot) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (snapshot.isForSameItemAs(snapshots.get(i))) {
                return i;
            }
        }
        throw new Defect("Cannot find match for " + snapshot);
    }

    public enum Column {
        ITEM_IDENTIFIER("Item") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return snapshot.itemId;
            }
        },
        LAST_PRICE("Last Price") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return snapshot.lastPrice;
            }
        },
        LAST_BID("Last Bid") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return snapshot.lastBid;
            }
        },
        SNIPER_STATUS("State") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return textFor(snapshot.state);
            }
        };

        Column(String name) {
            this.name = name;
        }

        public final String name;

        public static Column at(int offset) {
            return values()[offset];
        }

        abstract public Object valueIn(SniperSnapshot snapshot);
    }
}
