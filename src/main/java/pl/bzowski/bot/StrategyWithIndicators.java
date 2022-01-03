package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;

import java.util.Arrays;

public class StrategyWithIndicators extends BaseStrategy {

    private final Indicator[] indicators;
    public Logger logger = LoggerFactory.getLogger(StrategyWithIndicators.class);

    public StrategyWithIndicators(String name, Rule entryRule, Rule exitRule, Indicator... indicators) {
        super(name, entryRule, exitRule);
        this.indicators = indicators;
    }

    @Override
    public boolean shouldEnter(int index) {
        boolean shouldEnter = super.shouldEnter(index);
        logger.info("Strategy {} should enter: {}. Indicators:", getName(), shouldEnter);
        Arrays.stream(indicators).forEach(i -> logger.info("Indicator {} - value: {}", i.getClass(), i.getValue(index)));
        return shouldEnter;
    }

    public Indicator getIndicator(Class<? extends Indicator> indicatorClass) {
        return Arrays.stream(indicators).filter(indicator -> indicator.getClass().equals(indicatorClass)).findFirst().orElseThrow();
    }
}
