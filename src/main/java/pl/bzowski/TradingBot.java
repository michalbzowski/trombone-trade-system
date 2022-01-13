package pl.bzowski;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.bot.*;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.command.APICommandFactory;


import pl.bzowski.bot.commands.*;
import pl.bzowski.bot.positions.ClosePosition;
import pl.bzowski.bot.positions.OpenPosition;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.response.AllSymbolsResponse;
import pro.xstore.api.message.response.LoginResponse;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData.ServerEnum;
import pro.xstore.api.sync.SyncAPIConnector;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TradingBot {

    static Logger logger = LoggerFactory.getLogger(TradingBot.class);

    //VARIABLES :P
    static PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M1;
    static Set<String> symbols = new HashSet<>();

    public static void main(String[] args) throws Exception {
        logger.info("Version: {}", 0.5);
        String LOGIN = "";
        String PASSWORD = "";
        //please provide the application details if you received them
        String APP_ID = "x";
        String APP_NAME = "x";
        Credentials credentials = new Credentials(LOGIN, PASSWORD, APP_ID, APP_NAME);
        SyncAPIConnector connector = new SyncAPIConnector(ServerEnum.DEMO);
        LoginResponse loginResponse = APICommandFactory.executeLoginCommand(connector, credentials);
        if (loginResponse.getStatus()) {
            symbols.add("GBPCHF");
            symbols.add("EURUSD");
            symbols.add("USDJPY");
            symbols.add("DE30");
            // symbols = Set.of("POLKADOT", "DOGECOIN", "CHAINLINK", "STELLAR", "BITCOIN");
//            symbols = Set.of("BITCOIN");
            AllSymbolsResponse allSymbolsResponse = APICommandFactory.executeAllSymbolsCommand(connector);
            SeriesHandler seriesHandler = new SeriesHandler(periodCode);

            Set<SymbolRecord> filteredSymbolRecords = allSymbolsResponse.getSymbolRecords()
                    .stream()
                    .filter(SymbolRecord::isCurrencyPair)
                    .filter(sr -> symbols.contains(sr.getSymbol()))
                    .collect(Collectors.toSet());
            Map<String, BotInstanceForSymbol> bots =
                    new HashMap<>();

            ChartRangeCommand chartRangeCommand = new ChartRangeCommand(connector);
            TradeTransactionCommand tradeTransactionCommand = new TradeTransactionCommand(connector);
            SymbolCommand symbolCommand = new SymbolCommand(connector);
            TradeTransactionStatusCommand tradeTransactionStatusCommand = new TradeTransactionStatusCommand(connector);
            TradesCommand tradesCommand = new TradesCommand(connector);
            OpenPosition openPosition = new OpenPosition(tradeTransactionCommand, symbolCommand, tradeTransactionStatusCommand, tradesCommand);
            ClosePosition closePosition = new ClosePosition(tradesCommand, tradeTransactionCommand, tradeTransactionStatusCommand);

            BotFactory botFactory = new BotFactory(periodCode, seriesHandler, chartRangeCommand, openPosition, closePosition);
            for (SymbolRecord symbolRecord : filteredSymbolRecords) {
                BotInstanceForSymbol botInstance = botFactory.createBotInstance(symbolRecord);
                bots.put(symbolRecord.getSymbol(), botInstance);
            }

            ShutdownHook.shutdownHook(connector, bots);

            runBot(connector, bots, seriesHandler);
        }
    }

    private static void runBot(SyncAPIConnector connector, Map<String, BotInstanceForSymbol> strategies, SeriesHandler seriesHandler) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TradeBotStreamListener tradeBotStreamListener = new TradeBotStreamListener(strategies, seriesHandler);
            try {
                connector.connectStream(tradeBotStreamListener);
                strategies.forEach((key, value) -> {
                    try {
                        connector.subscribeCandle(key);
                    } catch (APICommunicationException e) {
                        logger.error(e.getLocalizedMessage());
                    }

                });
            } catch (IOException | APICommunicationException e) {
                logger.error(e.getLocalizedMessage());
            }
        });
    }
}