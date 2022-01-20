package pl.bzowski.trader;

import pl.bzowski.trader.balance.Balance;
import pl.bzowski.trader.strategies.StochasticOscilatorStrategy;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.STickRecord;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.SymbolResponse;
import pro.xstore.api.sync.SyncAPIConnector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlmostMockedTrader implements Trader {

    private double ask;
    private double bid;
    private final SyncAPIConnector connector;
    private final Balance balance;
    private final Map<UUID, MyPosition> openedPositions = new HashMap<>();

    public AlmostMockedTrader(SyncAPIConnector connector, Balance balance) {
        this.connector = connector;
        this.balance = balance;
    }

//
//    Należy również pamietać, że
//    przy otwieraniu pozycji długiej, transakcja
//    zostanie otwarta po cenie kupna (ask),
//    a zamknięta po cenie sprzedaży (bid).
//
//    Z drugiej strony, krótką pozycję
//    otworzy się po cenie sprzedaży (bid),
//    a zamknie po cenie kupna (ask).


    @Override
    public UUID openBuyPosition(String symbol, double positionVolume, StochasticOscilatorStrategy stochasticOscilatorStrategy) throws APIErrorResponse, APICommunicationException, APIReplyParseException, APICommandConstructionException {
        SymbolResponse symbolResponse = APICommandFactory.executeSymbolCommand(connector, symbol);
        SymbolRecord symbolRecord = symbolResponse.getSymbol();
        double spreadRaw = symbolRecord.getSpreadRaw(); //, ponieważ na początku będzie to tester strategii a nie mam dostępnu do danych historycznych, to spread pobiorę aktualny
        double spreadTable = symbolRecord.getSpreadTable();
        UUID key = UUID.randomUUID();
        double price = ask * positionVolume;
        openedPositions.put(key, new MyPosition(symbol, new Short(), price, price + (price * 0.02), price - (price * 0.02) * 4, stochasticOscilatorStrategy));
        balance.minus(spreadRaw);
        return key;
    }

    @Override
    public UUID openSellPosition(String symbol, double positionVolume, StochasticOscilatorStrategy stochasticOscilatorStrategy) throws APICommandConstructionException, APIErrorResponse, APICommunicationException, APIReplyParseException {
        SymbolResponse symbolResponse = APICommandFactory.executeSymbolCommand(connector, symbol);
        SymbolRecord symbolRecord = symbolResponse.getSymbol();
        double spreadRaw = symbolRecord.getSpreadRaw(); //, ponieważ na początku będzie to tester strategii a nie mam dostępnu do danych historycznych, to spread pobiorę aktualny
        double spreadTable = symbolRecord.getSpreadTable();
        UUID key = UUID.randomUUID();
        double price = bid * positionVolume;
        openedPositions.put(key, new MyPosition(symbol, new Short(), price, price + (price * 0.02), price - (price * 0.02) * 4, stochasticOscilatorStrategy));
        balance.minus(spreadRaw);
        return key;
    }

    @Override
    public void closePosition(UUID positionId) {

    }


    @Override
    public void handle(STickRecord tickRecord) {
        this.ask = tickRecord.getAsk();
        this.bid = tickRecord.getBid();
        openedPositions.values().forEach(p -> {
            p.checkPosition(ask, bid, balance);
        });
    }
}
