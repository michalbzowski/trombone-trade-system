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

public class SimpleLongSarEma200Strategy implements StrategyBuilder {

  // dodac stop loss na poziomie parabolicSar oraz take Profit 1.5
  // dopasuj SL do r/r, bo czasem SAR jest bardzo daleko od ceny
  // Parabolic SAR Divergence - kiedy cena podąża w przeciwnym kierunku niż
  // indykator
  // Wtedy istnieje szansa, że rysowany przez cenę trend będzie kontynuowany
  // //TODO
  // Cena idzie do góry = bullish divergence
  // Cena idzie w dół = bearish divergence
  // Sygnałem jest przejście SAR na drugą stronę wykresu
  // buy otwiera się po cenie ask a ja w seirach mam ceny close - bid
  @Override
  public StrategyWithLifeCycle buildStrategy(BarSeries series) {
    if (series == null) {
      throw new IllegalArgumentException("Series cannot be null");
    }
    ParabolicSarIndicator parabolicSarIndicator = new ParabolicSarIndicator(series);
    ClosePriceIndicator cpi = new ClosePriceIndicator(series);
    EMAIndicator ema200 = new EMAIndicator(cpi, 200);

    Rule enterRule = new CrossedDownIndicatorRule(parabolicSarIndicator, cpi)
        .and(new OverIndicatorRule(cpi, ema200));
    Rule exitRule = new CrossedUpIndicatorRule(parabolicSarIndicator, cpi);
    return new StrategyWithLifeCycle("SIMPLE-SAR+EMA200-LONG", enterRule, exitRule, parabolicSarIndicator, cpi,
        ema200);
  }

}