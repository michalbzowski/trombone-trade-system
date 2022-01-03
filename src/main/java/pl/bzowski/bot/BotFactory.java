package pl.bzowski.bot;

import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.ChartRangeInfoRecord;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.ChartResponse;
import pro.xstore.api.sync.SyncAPIConnector;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class BotFactory {

    private static final long ONE_DAY = 86_400_000;
    private static final long FOUR_HOURS = Duration.ofHours(4).toMillis();

    public static Function<SymbolRecord, BotInstanceForSymbol> createBotInstanceForSymbol(PERIOD_CODE periodCode, SyncAPIConnector connector) {
        return symbolRecord -> {
            try {
                String symbol = symbolRecord.getSymbol();
                ChartResponse chartResponse = getArchiveCandles(symbol, connector, periodCode);
                return new BotInstanceForSymbol(symbol,
                        symbolRecord.getSpreadRaw(),
                        chartResponse.getDigits(),
                        chartResponse.getRateInfos(),
                        periodCode,
                        enterContext -> EnterPosition.enterPosition(connector, enterContext),
                        enterContext -> ClosePosition.closePosition(connector, enterContext)
                );
            } catch (APIErrorResponse | APICommunicationException | APIReplyParseException | APICommandConstructionException apiErrorResponse) {
                apiErrorResponse.printStackTrace();
            }
            return null;
        };
    }

    private static ChartResponse getArchiveCandles(String symbol, SyncAPIConnector connector, PERIOD_CODE periodCode) throws APIErrorResponse, APICommunicationException, APIReplyParseException, APICommandConstructionException {
        long NOW = System.currentTimeMillis();
        ChartRangeInfoRecord record = new ChartRangeInfoRecord(symbol, periodCode, NOW - FOUR_HOURS, NOW);
        return APICommandFactory.executeChartRangeCommand(connector, record);
    }
}