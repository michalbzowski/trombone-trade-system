package pl.bzowski.trader;

import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.STickRecord;

import java.util.Random;
import java.util.function.Consumer;

public class CandleToTicksConverter {
    private long MINUTE_IN_MILISECONDS = 60_000L;
    private final long candleIntervalInMiliseconds;
    private String symbol;
    private RateInfoRecord currentCandle;
    Random random = new Random();

    public CandleToTicksConverter(RateInfoRecord currentCandle, PERIOD_CODE periodCode, String symbol) {
        this.currentCandle = currentCandle;
        this.candleIntervalInMiliseconds = convertPeriodCodeToMiliseconds(periodCode);
        this.symbol = symbol;
    }

    private long convertPeriodCodeToMiliseconds(PERIOD_CODE periodCode) {
        return periodCode.getCode() * MINUTE_IN_MILISECONDS;
    }

    public void tickStream(Consumer<STickRecord> consumer) {
        double vol = this.currentCandle.getVol();
        STickRecord[] ticks = new STickRecord[(int) Math.floor(vol)];


        long timestampShift = (long) Math.floor(candleIntervalInMiliseconds / vol);
        long partialVolume = (long) (vol / timestampShift);

        ticks[0] = openTickRecord(currentCandle, partialVolume);


        for (int i = 1; i < ticks.length - 2; i++) {
            ticks[i] = randomValueBetweenLowAndHigh(currentCandle, timestampShift, random, partialVolume);
        }

        ticks[ticks.length - 1] = closeTickRecord(currentCandle, partialVolume);
        for (int i = 0; i < ticks.length - 2; i++) {
            STickRecord tick = ticks[i];
            consumer.accept(tick);
        }
    }

    /*
    Jeżeli mam świece o czasie trwania I oraz wolumen w tej świecy V (liczba zmian cen) to muszę zmieściś wszystkie
    zmieny tej ceny w ciągu I.

     */
    private STickRecord randomValueBetweenLowAndHigh(RateInfoRecord currentCandle, double timestampShift, Random random, long volume) {
        double leftLimit = currentCandle.getOpen() + currentCandle.getHigh();
        double rightLimit = currentCandle.getOpen() + currentCandle.getLow();
        double price = leftLimit + random.nextDouble() * (rightLimit - leftLimit);


        long timestamp = (long) (currentCandle.getCtm() + timestampShift);
        return STickRecord.create(price, price, symbol, timestamp, volume);
    }

    private STickRecord closeTickRecord(RateInfoRecord currentCandle, long volume) {
        return STickRecord.create(currentCandle.getClose(), currentCandle.getClose(), symbol, currentCandle.getCtm() + candleIntervalInMiliseconds, volume);
    }

    private STickRecord openTickRecord(RateInfoRecord currentCandle, long volume) {
        return STickRecord.create(currentCandle.getOpen(), currentCandle.getOpen(), symbol, currentCandle.getCtm(), volume);
    }
}
