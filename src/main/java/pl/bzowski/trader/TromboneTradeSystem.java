package pl.bzowski.trader;

import pl.bzowski.trader.balance.Balance;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.TickRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.TickPricesResponse;
import pro.xstore.api.streaming.StreamingListener;
import pro.xstore.api.sync.Server;
import pro.xstore.api.sync.ServerData;
import pro.xstore.api.sync.SyncAPIConnector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

public class TromboneTradeSystem {


    private final StreamingListener streamingListener;
    private final TickRepository tickRepository;
    private final Trader trader;
    private final Balance balance;
    private final SyncAPIConnector connector;


    public TromboneTradeSystem(SyncAPIConnector connector, TickRepository tickRepository, XStationStreamingListener streamingListener) {
        this.connector = connector;
        this.tickRepository = tickRepository;
        this.streamingListener = streamingListener;
        this.balance = new Balance();
        this.trader = new AlmostMockedTrader(connector, balance);

    }

    public void runSystem() throws Exception {
//
//        connector.connectStream(streamingListener);
//        connector.subscribeBalance();
//        connector.subscribeCandle(SYMBOL);
//        connector.subscribeTickPrices(SYMBOL);
//        connector.subscribeKeepAlive();
//        Thread.sleep(1600);
//        connector.unsubscribeKeepAlive();
//        connector.unsubscribeCandle(SYMBOL);
//        connector.unsubscribeTickPrices(SYMBOL);
//        connector.unsubscribeBalance();
//        connector.disconnectStream();
        System.out.println("\nStream disconnected.");
    }

    private void extracted(SyncAPIConnector connector) throws APICommandConstructionException, APICommunicationException, APIReplyParseException, APIErrorResponse, IOException, InterruptedException {


        LinkedList<String> list = new LinkedList<>();
        String symbol = "DE30";
        list.add(symbol);

        TickPricesResponse resp = APICommandFactory.executeTickPricesCommand(connector, 0L, list, 0L);
        for (TickRecord tr : resp.getTicks()) {
            System.out.println("TickPrices result: " + tr.getSymbol() + " - ask: " + tr.getAsk());
        }

        connector.connectStream(streamingListener);
        System.out.println("Stream connected.");

        connector.subscribePrice(symbol);
        connector.subscribeTrades();

        Thread.sleep(10000);

        connector.unsubscribePrice(symbol);
        connector.unsubscribeTrades();

        connector.disconnectStream();
        System.out.println("Stream disconnected.");

        Thread.sleep(5000);

        connector.connectStream(streamingListener);
        System.out.println("Stream connected again.");
        connector.disconnectStream();
        System.out.println("Stream disconnected again.");
        System.exit(0);
    }

    protected Map<String, Server> getAvailableServers() {
        return ServerData.getProductionServers();
    }
}