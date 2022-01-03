package pl.bzowski.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.xstore.api.message.records.*;
import pro.xstore.api.streaming.StreamingListener;

import java.util.ArrayList;
import java.util.Collection;

public class XStationStreamingListener extends StreamingListener {

    Logger logger = LoggerFactory.getLogger(XStationStreamingListener.class);

    private Collection<XStationEventHandler> handlers = new ArrayList<>();

    @Override
    public void receiveTradeRecord(STradeRecord tradeRecord) {
        logger.info("Stream trade record: " + tradeRecord);
    }

    @Override
    public void receiveTickRecord(STickRecord tickRecord) {
        logger.info("\nStream tick record: " + tickRecord);
        handlers.forEach(xStationEventHandler -> xStationEventHandler.handle(tickRecord));
    }

    @Override
    public void receiveBalanceRecord(SBalanceRecord balanceRecord) {
        logger.info("\nStream balance record: " + balanceRecord);
    }

    @Override
    public void receiveCandleRecord(SCandleRecord candleRecord) {
        logger.info("\nStream candle record: " + candleRecord);
    }

    @Override
    public void receiveKeepAliveRecord(SKeepAliveRecord keepAliveRecord) {
        logger.info("\n" + keepAliveRecord);
    }

    public void registerHandler(XStationEventHandler handler) {
        this.handlers.add(handler);
    }
}
