package pl.bzowski.bot.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class SimpleShortSarEma200Strategy implements StrategyBuilder {

  @Override
  public StrategyWithLifeCycle buildStrategy(BarSeries series) {
    if (series == null) {
      throw new IllegalArgumentException("Series cannot be null");
    }
    ParabolicSarIndicator parabolicSarIndicator = new ParabolicSarIndicator(series);
    ClosePriceIndicator cpi = new ClosePriceIndicator(series);
    EMAIndicator ema200 = new EMAIndicator(cpi, 200);

    Rule enterRule = new CrossedUpIndicatorRule(parabolicSarIndicator, cpi).and(new UnderIndicatorRule(cpi, ema200));
    Rule exitRule = new CrossedDownIndicatorRule(parabolicSarIndicator, cpi);
    return new StrategyWithLifeCycle("SIMPLE-SAR+EMA200-SHORT", enterRule, exitRule, parabolicSarIndicator, cpi,
        ema200); // ONLY SHORT
  }

}
