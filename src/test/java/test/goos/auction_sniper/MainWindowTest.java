package test.goos.auction_sniper;

import com.objogate.wl.swing.probe.ValueMatcherProbe;
import goos.auction_sniper.Item;
import goos.auction_sniper.ui.MainWindow;
import goos.auction_sniper.SniperPortfolio;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class MainWindowTest {
    private final SniperPortfolio portfolio = new SniperPortfolio();
    private final MainWindow mainWindow = new MainWindow(portfolio);

    private final AuctionSniperDriver driver = new AuctionSniperDriver(1000);

    @Test
    public void makesUserRequestWhenJoinButtonClicked() {
        final ValueMatcherProbe<Item> itemProbe = new ValueMatcherProbe<>(
                equalTo(Item.create("some item-id", 789)), "join request");

        mainWindow.addUserRequestListener(item -> itemProbe.setReceivedValue(item));

        // This fails when using "an item-id" as suggested by the book; the "a" gets eaten for some reason
        // (Might be something that only happens on my machine?)
        // —Eduardo Dobay
        driver.startBiddingFor("some item-id", 789);
        driver.check(itemProbe);
    }
}
