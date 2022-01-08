package pl.bzowski.bot.strategies;

import org.ta4j.core.BarSeries;

public interface StrategyBuilder {
 StrategyWithLifeCycle buildStrategy(BarSeries series);
}
