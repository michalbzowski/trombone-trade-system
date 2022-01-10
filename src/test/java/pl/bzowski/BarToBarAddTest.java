package pl.bzowski;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import pl.bzowski.bot.SeriesHandler;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class BarToBarAddTest {

    /*
    To wygląda tak, że bieżąca świeczka minutowa dopiero się rysuje. Websocket API zwraca raz na minutę świeczkę, która ma
    cte = now - 1m
    Czyli jeśli jest 12:03:53 to świeczka ta przyjdzie z api dopiero o 12:04:00 i bedzie miec cte = 12:03:00

    Jeśli gramy na minutowych, to każdy candleReord przechodzący przez seriesHandles powinien przechodzić z nowym barem

     */
    @Test
    public void lol() {
        SeriesHandler seriesHandler = new SeriesHandler(PERIOD_CODE.PERIOD_M1);
        BarSeries usdJpyBarSeries = seriesHandler.createSeries("USDJPY");
        List<RateInfoRecord> candles = generateRateInfoRecord();
        int digits = 3;
        seriesHandler.fillSeries(candles, digits, usdJpyBarSeries);

        SCandleRecord candleRecord = new SCandleRecordInTest();
        candleRecord.setIn
        seriesHandler.update();
    }

    private List<RateInfoRecord> generateRateInfoRecord() {
        List<RateInfoRecord> list = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.of(2020, 12, 1, 12, 15, 0, 0, ZoneId.of("UTC"));
        list.add(new RateInfoRecord(now.toEpochSecond(), 0.0, 0.0, 0.0, 0.0, 1));
        return list;
    }

}
