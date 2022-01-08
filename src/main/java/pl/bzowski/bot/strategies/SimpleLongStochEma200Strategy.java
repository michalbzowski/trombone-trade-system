package pl.bzowski.bot.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class SimpleLongStochEma200Strategy implements StrategyBuilder {

  @Override
  public StrategyWithLifeCycle buildStrategy(BarSeries series) {
    if (series == null) {
      throw new IllegalArgumentException("Series cannot be null");
    }
    StochasticOscillatorKIndicator kIndicator = new StochasticOscillatorKIndicator(series, 13);
    StochasticOscillatorDIndicator dIndicator = new StochasticOscillatorDIndicator(kIndicator);

    ClosePriceIndicator cpi = new ClosePriceIndicator(series);
    EMAIndicator ema200 = new EMAIndicator(cpi, 200);

    Rule enterRule = new CrossedUpIndicatorRule(kIndicator, dIndicator)
        .and(new UnderIndicatorRule(kIndicator, 80))// wejscie w praktycznie kazde skrzyzowanie linie
        .and(new UnderIndicatorRule(dIndicator, 80))
        .and(new OverIndicatorRule(cpi, ema200));
    Rule exitRule = new UnderIndicatorRule(kIndicator, 20);
    return new StrategyWithLifeCycle("SIMPLE-STOCH+EMA200-LONG", enterRule, exitRule, ema200, cpi);
  }

}