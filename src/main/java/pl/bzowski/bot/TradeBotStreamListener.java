package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.xstore.api.message.records.SCandleRecord;
import pro.xstore.api.streaming.StreamingListener;

import java.util.Map;

public class TradeBotStreamListener extends StreamingListener {

    private final Map<String, IchimokuTrendAndSignalBot> strategies;
    private final MinuteSeriesHandler minuteSeriesHandler;
    Logger logger = LoggerFactory.getLogger(TradeBotStreamListener.class);

    public TradeBotStreamListener(Map<String, IchimokuTrendAndSignalBot> strategies, MinuteSeriesHandler minuteSeriesHandler) {
        this.strategies = strategies;
        this.minuteSeriesHandler = minuteSeriesHandler;
    }

    @Override
    public void receiveCandleRecord(SCandleRecord candleRecord) {
        logger.info("Stream candle record: " + candleRecord);
        IchimokuTrendAndSignalBot ichimokuTrendAndSignalBot = strategies.get(candleRecord.getSymbol());
        int endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);
        if (endIndex > 0) { //Co 4 godziny warunek będzie spełniony
            ichimokuTrendAndSignalBot.onTick(endIndex);
        }
    }


}
