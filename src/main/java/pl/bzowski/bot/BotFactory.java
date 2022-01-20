package pl.bzowski.bot;

import org.ta4j.core.BarSeries;
import pl.bzowski.bot.commands.ChartRangeCommand;
import pl.bzowski.bot.positions.ClosePosition;
import pl.bzowski.bot.positions.OpenPosition;
import pl.bzowski.bot.trend.TrendChecker;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.ChartRangeInfoRecord;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.ChartResponse;

import java.time.Duration;

public class BotFactory {

    private static final long TWO_WEEK = Duration.ofDays(14).toMillis();
    private static final long FOUR_HOURS = Duration.ofHours(4).toMillis();

    private final MinuteSeriesHandler minuteSeriesHandler;
    private final OpenPosition openPosition;
    private final ClosePosition closePosition;
    private final ChartRangeCommand chartRangeCommand;
    private final TrendChecker trendChecker;

    public BotFactory(MinuteSeriesHandler minuteSeriesHandler, ChartRangeCommand chartRangeCommand, OpenPosition openPosition, ClosePosition closePosition) {
        this.minuteSeriesHandler = minuteSeriesHandler;
        this.chartRangeCommand = chartRangeCommand;
        this.openPosition = openPosition;
        this.closePosition = closePosition;
        this.trendChecker = new TrendChecker(minuteSeriesHandler);
    }

    public IchimokuTrendAndSignalBot createBotInstance(SymbolRecord symbolRecord) {
        try {
            String symbol = symbolRecord.getSymbol();
            BarSeries minuteSeries = minuteSeriesHandler.createFourHoursSeries(symbol);
            ChartResponse chartResponse = getArchiveCandles(symbol);
            minuteSeriesHandler.fillFourHoursSeries(chartResponse.getRateInfos(), chartResponse.getDigits(), minuteSeries);
            return new IchimokuTrendAndSignalBot(symbol, minuteSeries, trendChecker);
        } catch (APIErrorResponse | APICommunicationException | APIReplyParseException
                | APICommandConstructionException apiErrorResponse) {
            apiErrorResponse.printStackTrace();
        }
        return null;
    }

    private ChartResponse getArchiveCandles(String symbol)
            throws APIErrorResponse, APICommunicationException, APIReplyParseException,
            APICommandConstructionException {
        long NOW = System.currentTimeMillis();
        ChartRangeInfoRecord record = new ChartRangeInfoRecord(symbol,  PERIOD_CODE.PERIOD_H4, NOW - TWO_WEEK, NOW);//tu minimum 52 TYGODNIE!!!
        return chartRangeCommand.execute(record);
    }
}