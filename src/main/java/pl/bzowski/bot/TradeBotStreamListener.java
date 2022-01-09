package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.xstore.api.message.records.SCandleRecord;
import pro.xstore.api.streaming.StreamingListener;

import java.util.Map;

public class TradeBotStreamListener extends StreamingListener {

    Logger logger = LoggerFactory.getLogger(TradeBotStreamListener.class);

    private final Map<String, BotInstanceForSymbol> strategies;
    private final SeriesHandler seriesHandler;

    public TradeBotStreamListener(Map<String, BotInstanceForSymbol> strategies, SeriesHandler seriesHandler) {
        this.strategies = strategies;
        this.seriesHandler = seriesHandler;
    }

    @Override
    public void receiveCandleRecord(SCandleRecord candleRecord) {
        logger.info("Stream candle record: " + candleRecord);
        BotInstanceForSymbol botInstanceForSymbol = strategies.get(candleRecord.getSymbol());
        int endIndex = seriesHandler.update(candleRecord);
        if (endIndex > 0) {
            botInstanceForSymbol.onTick(endIndex);
        }
    }


}
