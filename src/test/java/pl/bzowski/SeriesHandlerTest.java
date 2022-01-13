package pl.bzowski;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import pl.bzowski.bot.SeriesHandler;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;
import static org.assertj.core.api.Assertions.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeriesHandlerTest {

    @Test
    public void shouldCreateOneSeriesForEverySymbol() {
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M15;
        SeriesHandler seriesHandler = new SeriesHandler(periodCode);
        ZonedDateTime now = TestContext.NOW_MOCK;
        TimeMachine timeMachine = new TimeMachine(now);

        BarSeries usdJpyBarSeries = seriesHandler.createSeries(TestContext.USD_JPY);

        BarSeries eurUsdBarSeries = seriesHandler.createSeries(TestContext.EUR_USD);

        BarSeries plnUsdBarSeries = seriesHandler.createSeries(TestContext.PLN_USD);

        seriesHandler.fillSeries(timeMachine.generateFullyEndedCandles(6, periodCode.getCode()), 3, usdJpyBarSeries);
        seriesHandler.fillSeries(timeMachine.generateFullyEndedCandles(10, periodCode.getCode()), 3, eurUsdBarSeries);
        seriesHandler.fillSeries(timeMachine.generateFullyEndedCandles(3, periodCode.getCode()), 3, plnUsdBarSeries);

        assertThat(usdJpyBarSeries.getEndIndex()).isEqualTo(5);
        assertThat(eurUsdBarSeries.getEndIndex()).isEqualTo(9);
        assertThat(plnUsdBarSeries.getEndIndex()).isEqualTo(2);
    }

    @Test
    public void whenOneMinuteCandleIsAddedToOneMinuteBarSeriesNewBarShouldBeCreated() {
        ZonedDateTime now = TestContext.NOW_MOCK;
        TimeMachine timeMachine = new TimeMachine(now);
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M1;
        SeriesHandler seriesHandler = new SeriesHandler(periodCode);
        String symbol = TestContext.USD_JPY;
        BarSeries usdJpyBarSeries = seriesHandler.createSeries(symbol);

        int howManyHistoricCandles = 1;
        List<RateInfoRecord> historicCandles = timeMachine.generateFullyEndedCandles(howManyHistoricCandles,
                periodCode.getCode());
        int digits = 3;
        seriesHandler.fillSeries(historicCandles, digits, usdJpyBarSeries);

        // when new candle arrived
        SCandleRecord newCandle = timeMachine.generateCandleRecord(symbol, periodCode);

        // then endIndex = 1
        int endIndex = seriesHandler.update(newCandle);

        assertThat(endIndex).isEqualTo(1);

    }

    @Test
    public void whenTwoOneMinuteCandlesWereAddedToOneOneMinuteCandleThenTwoNewBarsShouldBeAdded() {
        ZonedDateTime now = TestContext.NOW_MOCK;
        TimeMachine timeMachine = new TimeMachine(now);
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M1;
        SeriesHandler seriesHandler = new SeriesHandler(periodCode);
        String symbol = TestContext.USD_JPY;
        BarSeries usdJpyBarSeries = seriesHandler.createSeries(symbol);
        int howManyHistoricCandles = 1;
        List<RateInfoRecord> historicCandles = timeMachine.generateFullyEndedCandles(howManyHistoricCandles,
                periodCode.getCode());
        int digits = 3;
        seriesHandler.fillSeries(historicCandles, digits, usdJpyBarSeries);

        SCandleRecord candleRecord = timeMachine.generateCandleRecord(symbol, periodCode);
        int endIndex = seriesHandler.update(candleRecord);

        candleRecord = timeMachine.generateCandleRecord(symbol, periodCode);
        endIndex = seriesHandler.update(candleRecord);

        assertThat(endIndex).isEqualTo(2);
    }

    @Test
    public void WhenToOneFiveMinuteCandleOneOneMinuteCandleWasAddedNewBarShouldBeAdded() {
        ZonedDateTime now = ZonedDateTime.of(2020, 12, 1, 12, 15, 0, 0, ZoneId.of("UTC"));
        TimeMachine timeMachine = new TimeMachine(now);
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M5;
        SeriesHandler seriesHandler = new SeriesHandler(periodCode);
        String symbol = "USDJPY";
        BarSeries usdJpyBarSeries = seriesHandler.createSeries(symbol);
        int howManyHistoricCandles = 1;
        List<RateInfoRecord> historicCandles = timeMachine.generateFullyEndedCandles(howManyHistoricCandles,
                periodCode.getCode());
        int digits = 3;
        seriesHandler.fillSeries(historicCandles, digits, usdJpyBarSeries);

        SCandleRecord candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        int endIndex = seriesHandler.update(candleRecord);
        assertThat(endIndex).isEqualTo(1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = seriesHandler.update(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = seriesHandler.update(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = seriesHandler.update(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = seriesHandler.update(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = seriesHandler.update(candleRecord);
        assertThat(endIndex).isEqualTo(2);

    }

}
