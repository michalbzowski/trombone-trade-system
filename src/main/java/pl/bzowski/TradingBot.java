package pl.bzowski;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.bot.*;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.*;
import pro.xstore.api.message.response.*;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData.ServerEnum;
import pro.xstore.api.sync.SyncAPIConnector;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TradingBot {

    static Logger logger = LoggerFactory.getLogger(TradingBot.class);

    //VARIABLES :P
    static PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M1;
    static Set<String> symbols = new HashSet<>();


    public static void main(String[] args) throws Exception {
        String LOGIN = args[1];
        String PASSWORD = args[2];
        //please provide the application details if you received them
        String APP_ID = args[3];
        String APP_NAME = args[4];
        Credentials credentials = new Credentials(LOGIN, PASSWORD, APP_ID, APP_NAME);
        SyncAPIConnector connector = new SyncAPIConnector(ServerEnum.DEMO);
        LoginResponse loginResponse = APICommandFactory.executeLoginCommand(connector, credentials);
        if (loginResponse.getStatus()) {
            symbols.add("TEZOS");
            symbols.add("CHAINLINK");
            symbols.add("EOS");
            symbols.add("POLKADOT");
            AllSymbolsResponse allSymbolsResponse = APICommandFactory.executeAllSymbolsCommand(connector);
            Map<String, BotInstanceForSymbol> strategies = allSymbolsResponse.getSymbolRecords()
                    .stream()
                    .filter(SymbolRecord::isCurrencyPair)
//                    .filter(sr -> sr.getSpreadRaw() < 0.04d)
                    .filter(sr -> symbols.contains(sr.getSymbol()))
                    .collect(Collectors.toUnmodifiableMap(SymbolRecord::getSymbol, BotFactory.createBotInstanceForSymbol(periodCode, connector)));

            ShutdownHook.shutdownHook(connector, strategies);

            runBot(connector, strategies);
        }
    }

    private static void runBot(SyncAPIConnector connector, Map<String, BotInstanceForSymbol> strategies) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TradeBotStreamListener tradeBotStreamListener = new TradeBotStreamListener(strategies);
            try {
                connector.connectStream(tradeBotStreamListener);
                strategies.entrySet().forEach(entry -> {
                    try {
                        connector.subscribeCandle(entry.getKey());
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