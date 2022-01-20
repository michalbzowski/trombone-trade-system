package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import pl.bzowski.bot.strategies.StrategyWithLifeCycle;
import pl.bzowski.bot.strategies.ichimoku.NeutralTenkanKinjunCrossStrategy;
import pl.bzowski.bot.strategies.ichimoku.StrongTenkanKinjunCrossStrategy;
import pl.bzowski.bot.strategies.ichimoku.WeakTenkanKinjunCrossStrategy;
import pl.bzowski.bot.trend.Trend;
import pl.bzowski.bot.trend.TrendChecker;
import pro.xstore.api.message.codes.PERIOD_CODE;

import java.util.HashSet;
import java.util.Set;

public class IchimokuTrendAndSignalBot {

    private static final Logger logger = LoggerFactory.getLogger(IchimokuTrendAndSignalBot.class);
    private final String symbol;
    private final TrendChecker trendChecker;
    private final Set<StrategyWithLifeCycle> strategies = new HashSet<>();

    public IchimokuTrendAndSignalBot(String symbol, BarSeries series, TrendChecker trendChecker) {
        this.symbol = symbol;
        this.trendChecker = trendChecker;
        loadStrategies(series);

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

    private void loadStrategies(BarSeries series) {
        strategies.add(new StrongTenkanKinjunCrossStrategy(symbol).getLongStrategy(series));
        strategies.add(new StrongTenkanKinjunCrossStrategy(symbol).getShortStrategy(series));
        strategies.add(new NeutralTenkanKinjunCrossStrategy(symbol).getLongStrategy(series));
        strategies.add(new NeutralTenkanKinjunCrossStrategy(symbol).getShortStrategy(series));
        strategies.add(new WeakTenkanKinjunCrossStrategy(symbol).getLongStrategy(series));
        strategies.add(new WeakTenkanKinjunCrossStrategy(symbol).getShortStrategy(series));
    }

    public void onTick(int endIndex) {
        logger.info("4h TICK!!!!!!!!!!!");
        logMemoryUsage();
        Trend weaklyTrend = trendChecker.checkTrend(symbol, PERIOD_CODE.PERIOD_W1);
        logger.info("Weakly {} trend is: {}", symbol, weaklyTrend.toString());
        Trend dailyTrend = trendChecker.checkTrend(symbol, PERIOD_CODE.PERIOD_D1);
        logger.info("Daily {} trend is: {}", symbol, dailyTrend.toString());
        for (StrategyWithLifeCycle strategy : strategies) {
            boolean shouldEnter = strategy.shouldEnter(endIndex);
            boolean shouldExit = strategy.shouldExit(endIndex);
            logger.info("Strategy: {} should enter {} or exit {}", strategy.getName(), shouldEnter, shouldExit);
        }
    }

    private void logMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        logger.info("Memory usage {} MB", usedMB);
    }
}
