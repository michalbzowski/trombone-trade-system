package pl.bzowski;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;

public class TimeMachine {

  private ZonedDateTime now;

  public TimeMachine(ZonedDateTime now) {
    this.now = now;
  }

  public List<RateInfoRecord> generateFullyEndedCandles(int historicalCandlesCount, long candleDurationInMinutes) {
    List<RateInfoRecord> list = new ArrayList<>();
    Duration ofMinutes = Duration.ofMinutes(candleDurationInMinutes);
    for (int i = 1; i <= historicalCandlesCount; i++) {
      Instant instant = now.toInstant();
      long epochMilli = instant.toEpochMilli();
      list.add(new RateInfoRecord(epochMilli, 0.0, 0.0, 0.0, 0.0, 1));
      Duration multipliedBy = ofMinutes.multipliedBy(i);
      now = now.plus(multipliedBy);
    }
    return list;
  }

  public SCandleRecord generateCandleRecord(String symbol, PERIOD_CODE candleDurationInMinutes) {
    SCandleRecord candleRecord = new SCandleRecord();
    candleRecord.setCtm(now.toInstant().toEpochMilli());
    candleRecord.setSymbol(symbol);
    now = now.plus(Duration.ofMinutes(candleDurationInMinutes.getCode()));
    return candleRecord;
  }
}
