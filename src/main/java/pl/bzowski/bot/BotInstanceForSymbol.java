package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.pnl.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BotInstanceForSymbol {

    Logger logger = LoggerFactory.getLogger(BotInstanceForSymbol.class);

    private final BarSeries series;
    private List<RateInfoRecord> archiveCandles;
    private final PERIOD_CODE periodCode;
    private Consumer<EnterContext> enterPosition;
    private Consumer<EnterContext> closePosition;
    private final String symbol;
    BarSeriesManager seriesManager;
    Num earnings;
    Set<StrategyWithIndicators> strategies = new HashSet<>();
    private final GrossReturnCriterion grc = new GrossReturnCriterion();
    TradingRecord tradingRecord;

    public BotInstanceForSymbol(String symbol, double spreadRaw, int digits, List<RateInfoRecord> archiveCandles, PERIOD_CODE periodCode, Consumer<EnterContext> enterPosition, Consumer<EnterContext> closePosition) {
        this.symbol = symbol;
        this.series = new BaseBarSeries(symbol);

        this.archiveCandles = archiveCandles;
        this.periodCode = periodCode;
        this.enterPosition = enterPosition;
        this.closePosition = closePosition;
        // Getting the bar series
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

        this.strategies.add(buildLongStrategy(series, spreadRaw));
        this.strategies.add(buildShortStrategy(series));
        logger.info(symbol);
        //BACKTEST
        // Running our juicy trading strategy...
        for (Strategy strategy : strategies) {
            logger.info("Strategy name: {}", strategy.getName());
            BarSeriesManager manager = new BarSeriesManager(series);
            TradingRecord tradingRecord = manager.run(strategy);
            logger.info("Number of trades for our strategy: " + tradingRecord.getPositionCount());

            // Initializing the trading history
            this.tradingRecord = new BaseTradingRecord();
            // Getting the profitable trades ratio
            AnalysisCriterion profitTradesRatio = new AverageProfitCriterion();
            logger.info("AverageProfitCriterion: " + profitTradesRatio.calculate(series, tradingRecord));
            AnalysisCriterion lossTradesRatio = new AverageLossCriterion();
            logger.info("AverageLossCriterion: " + lossTradesRatio.calculate(series, tradingRecord));
            AnalysisCriterion netProfitCriterion = new NetProfitCriterion();
            logger.info("netProfitCriterion: " + netProfitCriterion.calculate(series, tradingRecord));
            AnalysisCriterion netLossCriterion = new NetLossCriterion();
            logger.info("netLossCriterion: " + netLossCriterion.calculate(series, tradingRecord));
            AnalysisCriterion grossProfitCriterion = new GrossProfitCriterion();
            logger.info("grossProfitCriterion: " + grossProfitCriterion.calculate(series, tradingRecord));
            AnalysisCriterion grossLossCriterion = new GrossLossCriterion();
            logger.info("grossLossCriterion: " + grossLossCriterion.calculate(series, tradingRecord));
            AnalysisCriterion profitLossCriterion = new ProfitLossCriterion();
            logger.info("profitLossCriterion: " + profitLossCriterion.calculate(series, tradingRecord));
        }
        logger.info("Bars count: {}", series.getBarCount());
    }

    /*
     * We run the strategy
     */
    public void onTick(SCandleRecord record) {
        long ctm = record.getCtm();
        long code = periodCode.getCode();
        BaseBar newBar = getBaseBar(code, ctm, record.getClose(), record.getOpen(), record.getHigh(), record.getLow());
        series.addBar(newBar);

        int endIndex = series.getEndIndex();

        for (StrategyWithIndicators strategy : strategies) {
            if (strategy.shouldEnter(endIndex)) {
                // Our strategy should enter
                logger.info("Strategy " + strategy.getName() + " should ENTER on " + endIndex);
                enterPosition.accept(new EnterContext(strategy.getName(), record.getSymbol(), strategy, endIndex));
                boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
                if (entered) {
                    Trade entry = tradingRecord.getLastEntry();

                    logger.info("Entered on " + entry.getIndex() + " (price=" + entry.getNetPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                }
            } else if (strategy.shouldExit(endIndex)) {
                logger.info("Strategy " + strategy.getName() + "should EXIT on " + endIndex);
                closePosition.accept(new EnterContext(strategy.getName(), record.getSymbol(), strategy, endIndex));
                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
                if (exited) {
                    Trade exit = tradingRecord.getLastExit();
                    logger.info("Exited on " + exit.getIndex() + " (price=" + exit.getNetPrice().doubleValue()
                            + ", amount=" + exit.getAmount().doubleValue() + ")");
                }
            }


            this.earnings = grc.calculate(series, tradingRecord);
        }

    }

    private BaseBar getBaseBar(long code, long ctm, double close, double open, double high, double low) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ctm + code * 60_000), ZoneId.systemDefault());
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return BaseBar
                .builder()
                .closePrice(DecimalNum.valueOf(close))
                .openPrice(DecimalNum.valueOf(open))
                .highPrice(DecimalNum.valueOf(high))
                .lowPrice(DecimalNum.valueOf(low))
                .endTime(zonedDateTime)
                .timePeriod(Duration.ofMinutes(code))
                .build();
    }

    //dodac stop loss na poziomie parabolicSar oraz take Profit 1.5
    //dopasuj SL do r/r, bo czasem SAR jest bardzo daleko od ceny
    //Parabolic SAR Divergence - kiedy cena podąża w przeciwnym kierunku niż indykator
    //Wtedy istnieje szansa, że rysowany przez cenę trend będzie kontynuowany //TODO
    //Cena idzie do góry = bullish divergence
    //Cena idzie w dół = bearish divergence
    //Sygnałem jest przejście SAR na drugą stronę wykresu
//buy otwiera się po cenie ask a ja w seirach mam ceny close - bid
    public static StrategyWithIndicators buildLongStrategy(BarSeries series, double spreadRaw) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ParabolicSarIndicator parabolicSarIndicator = new ParabolicSarIndicator(series);
        ClosePriceIndicator cpi = new ClosePriceIndicator(series);
        EMAIndicator ema200 = new EMAIndicator(cpi, 200);

        Rule enterRule = new CrossedDownIndicatorRule(parabolicSarIndicator, cpi)
                .and(new OverIndicatorRule(parabolicSarIndicator, ema200));
        Rule exitRule = new CrossedUpIndicatorRule(parabolicSarIndicator, cpi);
        return new StrategyWithIndicators("SIMPLE-SAR+EMA200-LONG", enterRule, exitRule, parabolicSarIndicator, cpi, ema200);
    }

    public static StrategyWithIndicators buildShortStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ParabolicSarIndicator parabolicSarIndicator = new ParabolicSarIndicator(series);
        ClosePriceIndicator cpi = new ClosePriceIndicator(series);
        EMAIndicator ema200 = new EMAIndicator(cpi, 200);

        Rule enterRule = new CrossedUpIndicatorRule(parabolicSarIndicator, cpi)
                .and(new UnderIndicatorRule(parabolicSarIndicator, ema200));
        Rule exitRule = new CrossedDownIndicatorRule(parabolicSarIndicator, cpi);
        return new StrategyWithIndicators("SIMPLE-SAR+EMA200-SHORT", enterRule, exitRule, parabolicSarIndicator, cpi, ema200); //ONLY SHORT
    }


}
