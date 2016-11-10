package goos.auction_sniper.ui;

import goos.auction_sniper.Item;
import goos.auction_sniper.SniperPortfolio;
import goos.auction_sniper.UserRequestListener;
import goos.auction_sniper.util.Announcer;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class MainWindow extends JFrame {
    public static final String APPLICATION_TITLE = "Auction Sniper";

    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    private static final String SNIPERS_TABLE_NAME = "snipers table";
    public static final String NEW_ITEM_ID_NAME = "item id";
    public static final String NEW_ITEM_STOP_PRICE_NAME = "item stop price";
    public static final String JOIN_BUTTON_NAME = "join";

    private final Announcer<UserRequestListener> userRequests = Announcer.to(UserRequestListener.class);

    public MainWindow(SniperPortfolio portfolio) throws HeadlessException {
        super(APPLICATION_TITLE);
        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable(portfolio), makeControls());
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void fillContentPane(JTable snipersTable, JPanel controls) {
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(controls, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable(SniperPortfolio portfolio) {
        SnipersTableModel model = new SnipersTableModel();
        portfolio.addPortfolioListener(model);
        final JTable snipersTable = new JTable(model);
        snipersTable.setName(SNIPERS_TABLE_NAME);
        return snipersTable;

    }

    private JPanel makeControls() {
        JPanel controls = new JPanel(new FlowLayout());

        final JLabel itemIdLabel = new JLabel("Item:");
        controls.add(itemIdLabel);

        final JTextField itemIdField = new JTextField();
        itemIdField.setColumns(25);
        itemIdField.setName(NEW_ITEM_ID_NAME);
        controls.add(itemIdField);

        final JLabel stopPriceLabel = new JLabel("Stop Price:");
        controls.add(stopPriceLabel);

        final JFormattedTextField stopPriceField = new JFormattedTextField(NumberFormat.getInstance());
        stopPriceField.setColumns(25);
        stopPriceField.setName(NEW_ITEM_STOP_PRICE_NAME);
        controls.add(stopPriceField);

        JButton joinAuctionButton = new JButton("Join Auction");
        joinAuctionButton.setName(JOIN_BUTTON_NAME);
        joinAuctionButton.addActionListener(event -> {
            final String itemId = itemIdField.getText();
            final int stopPrice = ((Number) stopPriceField.getValue()).intValue();
            userRequests.announce().joinAuction(Item.create(itemId, stopPrice));
        });
        controls.add(joinAuctionButton);

        return controls;
    }

    public void addUserRequestListener(UserRequestListener listener) {
        userRequests.addListener(listener);
    }
}
