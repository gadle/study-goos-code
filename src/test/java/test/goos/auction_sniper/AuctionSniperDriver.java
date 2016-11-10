package test.goos.auction_sniper;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.*;
import com.objogate.wl.swing.gesture.GesturePerformer;
import goos.auction_sniper.ui.MainWindow;

import javax.swing.*;
import javax.swing.table.JTableHeader;

import static com.objogate.wl.swing.matcher.IterableComponentsMatcher.matching;
import static com.objogate.wl.swing.matcher.JLabelTextMatcher.withLabelText;

public class AuctionSniperDriver extends JFrameDriver {
    public AuctionSniperDriver(int timeoutMillis) {
        super(new GesturePerformer(),
                JFrameDriver.topLevelFrame(
                        named(MainWindow.MAIN_WINDOW_NAME),
                        showingOnScreen()),
                new AWTEventQueueProber(timeoutMillis, 100));
    }

    public void showsSniperStatus(String itemId, int lastPrice, int lastBid, String statusText) {
        JTableDriver table = new JTableDriver(this);
        table.hasRow(matching(
                withLabelText(itemId),
                withLabelText(String.valueOf(lastPrice)),
                withLabelText(String.valueOf(lastBid)),
                withLabelText(statusText)
        ));
    }

    public void hasColumnTitles() {
        JTableHeaderDriver headers = new JTableHeaderDriver(this, JTableHeader.class);
        headers.hasHeaders(matching(
                withLabelText("Item"),
                withLabelText("Last Price"),
                withLabelText("Last Bid"),
                withLabelText("State")
        ));
    }

    public void startBiddingFor(String itemId, int stopPrice) {
        // "replaceAllText" seems subject to some random bugs. Here "selectAll" seems to fix that.
        itemIdField().selectAll();
        itemIdField().replaceAllText(itemId);

        final JTextFieldDriver stopPriceField = textField(MainWindow.NEW_ITEM_STOP_PRICE_NAME);
        stopPriceField.selectAll();
        stopPriceField.replaceAllText(String.valueOf(stopPrice));

        bidButton().click();
    }

    private JTextFieldDriver itemIdField() {
        return textField(MainWindow.NEW_ITEM_ID_NAME);
    }

    private JTextFieldDriver textField(String name) {
        JTextFieldDriver field = new JTextFieldDriver(this, JTextField.class, named(name));
        field.focusWithMouse();
        return field;
    }

    private JButtonDriver bidButton() {
        return new JButtonDriver(this, JButton.class, named(MainWindow.JOIN_BUTTON_NAME));
    }
}
