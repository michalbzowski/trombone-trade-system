package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;

import java.time.*;
import java.util.HashMap;
import java.util.List;

public class SeriesHandler {

    private static final Logger logger = LoggerFactory.getLogger(SeriesHandler.class);

    private static final long MINUTE_IN_MILLISECONDS = 60_000L;
    private final PERIOD_CODE periodCode;
    private final HashMap<String, BarSeries> seriesMap;

    public SeriesHandler(PERIOD_CODE periodCode) {
        this.periodCode = periodCode;
        this.seriesMap = new HashMap<String, BarSeries>();
    }

    public BarSeries createSeries(String symbol) {
        BarSeries series = new BaseBarSeries(symbol);
        this.seriesMap.put(symbol, series);
        return seriesMap.get(symbol);
    }

    public void fillSeries(List<RateInfoRecord> archiveCandles, int digits, BarSeries series) {
        double divider = Math.pow(10, digits);
        archiveCandles.forEach(record -> {
            long ctm = record.getCtm();
            long code = periodCode.getCode();
            double close = (record.getOpen() + record.getClose()) / divider;
            double open = record.getOpen() / divider;
            double high = (record.getOpen() + record.getHigh()) / divider;
            double low = (record.getOpen() + record.getLow()) / divider;
            BaseBar bar = getBaseBar(code, ctm, close, open, high, low);
            series.addBar(bar);
        });
    }

    public int update(SCandleRecord record) {
        BarSeries series = seriesMap.get(record.getSymbol());
        //Ostatni bar z serii może być jeszcze w trakcie rysowania
        Bar lastBar = series.getLastBar();

        //jeśli tak jest to jego czas początkowy plus wybrany okres powinien być większy niż czas rekordu, który wpadł w tej chwili

        long ctm = record.getCtm();//Candle start time in CET time zone (Central European Time)
        long periodDurationInMilliseconds = periodCode.getCode() * MINUTE_IN_MILLISECONDS;
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ctm),
                ZoneId.systemDefault());
        ZonedDateTime currentOneMinuteBarBeginTime = localDateTime.atZone(ZoneId.systemDefault());
        BaseBar newBar = getBaseBar(PERIOD_CODE.PERIOD_M1.getCode(), ctm, record.getClose(), record.getOpen(), record.getHigh(), record.getLow());//Zawsze jednominutwa, bo taki tik dostajemy z appi

        ZonedDateTime lastBarBeginTime = lastBar.getBeginTime();
        ZonedDateTime plus = lastBarBeginTime.plus(Duration.ofMillis(periodDurationInMilliseconds));
        boolean isEquals = plus.isEqual(currentOneMinuteBarBeginTime);
        if (!isEquals) {
            series.addPrice(newBar.getClosePrice());
            logger.info("Series updated by another one minute candle");
            return -1; //It means that there is no new bar
        } else {
            logger.info("New bar added");
            series.addBar(newBar);
        }

        int endIndex = series.getEndIndex();
        return endIndex;
    }

    private BaseBar getBaseBar(long code, long ctm, double close, double open, double high, double low) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ctm + code * 60_000),
                ZoneId.systemDefault());
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return BaseBar.builder()
                .closePrice(DecimalNum.valueOf(close))
                .openPrice(DecimalNum.valueOf(open))
                .highPrice(DecimalNum.valueOf(high))
                .lowPrice(DecimalNum.valueOf(low))
                .endTime(zonedDateTime)
                .timePeriod(Duration.ofMinutes(code))
                .build();
    }

}
