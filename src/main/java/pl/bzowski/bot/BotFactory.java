package pl.bzowski.bot;

import org.ta4j.core.BarSeries;
import pl.bzowski.bot.positions.ClosePosition;
import pl.bzowski.bot.positions.OpenPosition;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.ChartRangeInfoRecord;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.ChartResponse;
import pro.xstore.api.sync.SyncAPIConnector;

import java.time.Duration;

public class BotFactory {

    private static final long ONE_DAY = 86_400_000 * 2;
    private static final long FOUR_HOURS = Duration.ofHours(4).toMillis();
    private final SyncAPIConnector connector;
    private final PERIOD_CODE periodCode;
    private final SeriesHandler seriesHandler;

    public BotFactory(SyncAPIConnector connector, PERIOD_CODE periodCode, SeriesHandler seriesHandler) {
        this.connector = connector;
        this.periodCode = periodCode;
        this.seriesHandler = seriesHandler;
    }

    public BotInstanceForSymbol createBotInstance(SymbolRecord symbolRecord) {
        try {
            String symbol = symbolRecord.getSymbol();
            BarSeries series = seriesHandler.createSeries(symbol);
            ChartResponse chartResponse = getArchiveCandles(symbol, connector, periodCode);
            seriesHandler.fillSeries(chartResponse.getRateInfos(), chartResponse.getDigits(), series);
            return new BotInstanceForSymbol(symbol,
                    series,
                    enterContext -> new OpenPosition().openPosition(connector, enterContext),
                    enterContext -> new ClosePosition().closePosition(connector, enterContext)
            );
        } catch (APIErrorResponse | APICommunicationException | APIReplyParseException | APICommandConstructionException apiErrorResponse) {
            apiErrorResponse.printStackTrace();
        }
        return null;
    }

    private static ChartResponse getArchiveCandles(String symbol, SyncAPIConnector connector, PERIOD_CODE periodCode) throws APIErrorResponse, APICommunicationException, APIReplyParseException, APICommandConstructionException {
        long NOW = System.currentTimeMillis();
        ChartRangeInfoRecord record = new ChartRangeInfoRecord(symbol, periodCode, NOW - ONE_DAY, NOW);
        return APICommandFactory.executeChartRangeCommand(connector, record);
    }
}