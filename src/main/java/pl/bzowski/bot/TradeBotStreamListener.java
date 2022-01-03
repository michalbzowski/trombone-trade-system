package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.xstore.api.message.records.SCandleRecord;
import pro.xstore.api.streaming.StreamingListener;

import java.util.Map;

public class TradeBotStreamListener extends StreamingListener {

    Logger logger = LoggerFactory.getLogger(TradeBotStreamListener.class);

    private final Map<String, BotInstanceForSymbol> strategies;


    public TradeBotStreamListener(Map<String, BotInstanceForSymbol> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void receiveCandleRecord(SCandleRecord candleRecord) {
        logger.info("Stream candle record: " + candleRecord);
        BotInstanceForSymbol botInstanceForSymbol = strategies.get(candleRecord.getSymbol());
        botInstanceForSymbol.onTick(candleRecord);
    }



}
