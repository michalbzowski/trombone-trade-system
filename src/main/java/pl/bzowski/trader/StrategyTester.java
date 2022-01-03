package pl.bzowski.trader;

import pl.bzowski.trader.balance.Balance;
import pl.bzowski.trader.chart.IntervalComboBox;
import pl.bzowski.trader.chart.StartTestJButton;
import pl.bzowski.trader.chart.SymbolJTextFiled;
import pl.bzowski.trader.chart.TradeSystemChart;
import pl.bzowski.trader.strategies.StochasticOscilatorStrategy;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.response.LoginResponse;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData.ServerEnum;
import pro.xstore.api.sync.SyncAPIConnector;

import java.awt.event.ActionListener;

public class StrategyTester {

    //please change the login and password below
    private static String LOGIN = "xxx";
    private static String PASSWORD = "xxx";

    //please provide the application details if you received them
    private static String APP_ID = "xxx";
    private static String APP_NAME = "xxx";

    public static void main(String[] args) throws Exception {

        Credentials credentials = new Credentials(LOGIN, PASSWORD, APP_ID, APP_NAME);
        SyncAPIConnector connector = new SyncAPIConnector(ServerEnum.DEMO);
        LoginResponse loginResponse = APICommandFactory.executeLoginCommand(connector, credentials);
        if (loginResponse.getStatus()) {


            SymbolJTextFiled symbolJTextFiled = new SymbolJTextFiled();
            IntervalComboBox intervalComboBox = new IntervalComboBox();

            XStationStreamingListener xStationStreamingListener = new XStationStreamingListener();
            ActionListener startTestActionListener = new StartTestAction(connector, xStationStreamingListener, symbolJTextFiled, intervalComboBox);
            StartTestJButton startTestJButton = new StartTestJButton(startTestActionListener);
            TradeSystemChart mainChart = new TradeSystemChart("Trombone Trade System",
                    "Actual Price Situation",
                    startTestJButton,
                    symbolJTextFiled,
                    intervalComboBox);



            Balance balance = new Balance();
            Trader trader = new AlmostMockedTrader(connector, balance);
            xStationStreamingListener.registerHandler(new StochasticOscilatorStrategy(trader));
            xStationStreamingListener.registerHandler(mainChart);
            xStationStreamingListener.registerHandler(trader);

        }
    }
}