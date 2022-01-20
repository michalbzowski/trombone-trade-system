package pl.bzowski;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import pl.bzowski.bot.MinuteSeriesHandler;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;
import static org.assertj.core.api.Assertions.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class MinuteSeriesHandlerTest {

    @Test
    public void shouldCreateOneSeriesForEverySymbol() {
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M15;
        MinuteSeriesHandler minuteSeriesHandler = new MinuteSeriesHandler();
        ZonedDateTime now = TestContext.NOW_MOCK;
        TimeMachine timeMachine = new TimeMachine(now);

        BarSeries usdJpyBarSeries = minuteSeriesHandler.createFourHoursSeries(TestContext.USD_JPY);

        BarSeries eurUsdBarSeries = minuteSeriesHandler.createFourHoursSeries(TestContext.EUR_USD);

        BarSeries plnUsdBarSeries = minuteSeriesHandler.createFourHoursSeries(TestContext.PLN_USD);

        minuteSeriesHandler.fillFourHoursSeries(timeMachine.generateFullyEndedCandles(6, periodCode.getCode()), 3, usdJpyBarSeries);
        minuteSeriesHandler.fillFourHoursSeries(timeMachine.generateFullyEndedCandles(10, periodCode.getCode()), 3, eurUsdBarSeries);
        minuteSeriesHandler.fillFourHoursSeries(timeMachine.generateFullyEndedCandles(3, periodCode.getCode()), 3, plnUsdBarSeries);

        assertThat(usdJpyBarSeries.getEndIndex()).isEqualTo(5);
        assertThat(eurUsdBarSeries.getEndIndex()).isEqualTo(9);
        assertThat(plnUsdBarSeries.getEndIndex()).isEqualTo(2);
    }

    @Test
    public void whenOneMinuteCandleIsAddedToOneMinuteBarSeriesNewBarShouldBeCreated() {
        ZonedDateTime now = TestContext.NOW_MOCK;
        TimeMachine timeMachine = new TimeMachine(now);
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M1;
        MinuteSeriesHandler minuteSeriesHandler = new MinuteSeriesHandler();
        String symbol = TestContext.USD_JPY;
        BarSeries usdJpyBarSeries = minuteSeriesHandler.createFourHoursSeries(symbol);

        int howManyHistoricCandles = 1;
        List<RateInfoRecord> historicCandles = timeMachine.generateFullyEndedCandles(howManyHistoricCandles,
                periodCode.getCode());
        int digits = 3;
        minuteSeriesHandler.fillFourHoursSeries(historicCandles, digits, usdJpyBarSeries);

        // when new candle arrived
        SCandleRecord newCandle = timeMachine.generateCandleRecord(symbol, periodCode);

        // then endIndex = 1
        int endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(newCandle);

        assertThat(endIndex).isEqualTo(1);

    }

    @Test
    public void whenTwoOneMinuteCandlesWereAddedToOneOneMinuteCandleThenTwoNewBarsShouldBeAdded() {
        ZonedDateTime now = TestContext.NOW_MOCK;
        TimeMachine timeMachine = new TimeMachine(now);
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M1;
        MinuteSeriesHandler minuteSeriesHandler = new MinuteSeriesHandler();
        String symbol = TestContext.USD_JPY;
        BarSeries usdJpyBarSeries = minuteSeriesHandler.createFourHoursSeries(symbol);
        int howManyHistoricCandles = 1;
        List<RateInfoRecord> historicCandles = timeMachine.generateFullyEndedCandles(howManyHistoricCandles,
                periodCode.getCode());
        int digits = 3;
        minuteSeriesHandler.fillFourHoursSeries(historicCandles, digits, usdJpyBarSeries);

        SCandleRecord candleRecord = timeMachine.generateCandleRecord(symbol, periodCode);
        int endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);

        candleRecord = timeMachine.generateCandleRecord(symbol, periodCode);
        endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);

        assertThat(endIndex).isEqualTo(2);
    }

    @Test
    public void WhenToOneFiveMinuteCandleOneOneMinuteCandleWasAddedNewBarShouldBeAdded() {
        ZonedDateTime now = ZonedDateTime.of(2020, 12, 1, 12, 15, 0, 0, ZoneId.of("UTC"));
        TimeMachine timeMachine = new TimeMachine(now);
        PERIOD_CODE periodCode = PERIOD_CODE.PERIOD_M5;
        MinuteSeriesHandler minuteSeriesHandler = new MinuteSeriesHandler();
        String symbol = "USDJPY";
        BarSeries usdJpyBarSeries = minuteSeriesHandler.createFourHoursSeries(symbol);
        int howManyHistoricCandles = 1;
        List<RateInfoRecord> historicCandles = timeMachine.generateFullyEndedCandles(howManyHistoricCandles,
                periodCode.getCode());
        int digits = 3;
        minuteSeriesHandler.fillFourHoursSeries(historicCandles, digits, usdJpyBarSeries);

        SCandleRecord candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        int endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);
        assertThat(endIndex).isEqualTo(1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);
        assertThat(endIndex).isEqualTo(-1);

        candleRecord  = timeMachine.generateCandleRecord(symbol, PERIOD_CODE.PERIOD_M1);
        endIndex = minuteSeriesHandler.updateFourHoursSeriesWithOneMinuteCandle(candleRecord);
        assertThat(endIndex).isEqualTo(2);

    }

}
