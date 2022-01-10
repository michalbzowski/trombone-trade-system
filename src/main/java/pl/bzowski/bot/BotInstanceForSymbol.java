package pl.bzowski.bot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.aggregator.DurationBarAggregator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import pl.bzowski.bot.positions.PositionContext;
import pl.bzowski.bot.strategies.ScalpingForBitcoin;
import pl.bzowski.bot.strategies.SimpleLongStochEma200Strategy;
import pl.bzowski.bot.strategies.StrategyWithLifeCycle;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;
import pro.xstore.api.message.records.TradeRecord;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class BotInstanceForSymbol {

    private final String symbol;
    private static final Logger logger = LoggerFactory.getLogger(BotInstanceForSymbol.class);

    private Function<PositionContext, Long> openPosition;
    private Function<PositionContext, Long> closePosition;
    private Set<StrategyWithLifeCycle> strategies = new HashSet<>();

    public BotInstanceForSymbol(String symbol,
            BarSeries series, Function<PositionContext, Long> openPosition,
            Function<PositionContext, Long> closePosition) {

        this.symbol = symbol;
        this.openPosition = openPosition;
        this.closePosition = closePosition;
        // Getting the bar series

        StrategyWithLifeCycle longStrategy = new SimpleLongStochEma200Strategy().getLongStrategy(series);
        strategies.add(longStrategy);

        StrategyWithLifeCycle shortStrategy = new SimpleLongStochEma200Strategy().getShortStrategy(series);
        strategies.add(shortStrategy);

        // BACKTEST
        // Running our juicy trading strategy...
        StrategyAnalysis sa = new StrategyAnalysis();
        for (Strategy strategy : strategies) {
            BarSeriesManager manager = new BarSeriesManager(series);
            TradingRecord tradingRecord = manager.run(strategy);
            sa.doIt(series, tradingRecord, strategy, symbol);
            // displayChart(series, strategy, tradingRecord.getPositions());
            // indiChart(symbol, strategy);
        }
    }

    /*
     * We run the strategy
     */
    public void onTick(int endIndex) {

        for (StrategyWithLifeCycle strategy : strategies) {
            PositionContext enterContext = new PositionContext(symbol, strategy, endIndex);
            loggingDebug(enterContext);
            logger.info("Strategy: {}", strategy.getName());
            boolean shouldEnter = strategy.shouldEnter(endIndex);
            logger.info("- should enter {} on index {}", shouldEnter, endIndex);
            ClosePriceIndicator cpi = (ClosePriceIndicator) strategy.getIndicator(ClosePriceIndicator.class);
            if (shouldEnter) {
                // Our strategy should enter
                logger.info("Should strategy {} ENTER for {} on {}?", strategy.getName(), symbol, endIndex);
                long id = this.openPosition.apply(enterContext);
                logger.info("Opened position id: {}", id);

                boolean entered = strategy.getTradingRecord().enter(endIndex, cpi.getValue(endIndex),
                        DecimalNum.valueOf(0.01));
                if (entered) {
                    Trade entry = strategy.getTradingRecord().getLastEntry();
                    System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getNetPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                }
            } else {
                logger.info("Should strategy {} CLOSE for {} on {}?", strategy.getName(), symbol, endIndex);
                if (strategy.shouldExit(endIndex)) {
                    long id = closePosition.apply(new PositionContext(symbol, strategy, endIndex));
                    logger.info("ID after close {}?", id);
                    boolean exited = strategy.getTradingRecord().exit(endIndex, cpi.getValue(endIndex),
                            DecimalNum.valueOf(0.01));
                    if (exited) {
                        Trade exit = strategy.getTradingRecord().getLastExit();
                        System.out
                                .println("Exited on " + exit.getIndex() + " (price=" + exit.getNetPrice().doubleValue()
                                        + ", amount=" + exit.getAmount().doubleValue() + ")");
                    }
                }
            }
        }
    }

    private void loggingDebug(PositionContext enterContext) {
        // logger.debug("Indicators - entering long {}:", enterContext.isLong());
        // logger.debug("Enter index: {}", enterContext.getEnterIndex());
        // logger.debug("EMA: {}",
        // enterContext.getIndicator(EMAIndicator.class).getValue(enterContext.getEnterIndex()));
        // logger.debug("SAR: {}",
        // enterContext.getIndicator(ParabolicSarIndicator.class).getValue(enterContext.getEnterIndex()));
        // logger.debug("CPI: {}",
        // enterContext.getIndicator(ClosePriceIndicator.class).getValue(enterContext.getEnterIndex()));
    }
}
