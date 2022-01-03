package pl.bzowski.trader.chart;

import pro.xstore.api.message.records.STickRecord;

import java.util.function.Consumer;

public class CandleCounter {


    private int timeInterval = 1;
    private STickRecord candelChartIntervalFirstPrint = null;
    private double open = 0.0;
    private double close = 0.0;
    private double low = 0.0;
    private double high = 0.0;
    private long volume = 0;

    public void count(STickRecord tickRecord, long candleInterval, Consumer<MyCandle> consumer) {

        double price = tickRecord.getAsk();
        if (candelChartIntervalFirstPrint != null) {
            long time = tickRecord.getTimestamp();
            if (timeInterval == (int) ((time / candleInterval) - (candelChartIntervalFirstPrint.getTimestamp() / candleInterval))) {
//                 Set the period close price
                close = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
                // Add new candle
                consumer.accept(new MyCandle(time, open, high, low, close, volume, tickRecord.getSymbol()));

                // Reset the intervalFirstPrint to null
                candelChartIntervalFirstPrint = null;
            } else {
                // Set the current low price
                if (MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT) < low)
                    low = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);

                // Set the current high price
                if (MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT) > high)
                    high = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);

                volume += tickRecord.getAskVolume();
            }
        } else {
            // Set intervalFirstPrint
            candelChartIntervalFirstPrint = tickRecord;
            // the first trade price in the day (day open price)
            open = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
            // the interval low
            low = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
            // the interval high
            high = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
            // set the initial volume
            volume = tickRecord.getAskVolume();
        }
    }
}
