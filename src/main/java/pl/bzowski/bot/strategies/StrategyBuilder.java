package pl.bzowski.bot.strategies;

import org.ta4j.core.BarSeries;

public interface StrategyBuilder {
    StrategyWithLifeCycle getLongStrategy(BarSeries series);

    StrategyWithLifeCycle getShortStrategy(BarSeries series);
}
