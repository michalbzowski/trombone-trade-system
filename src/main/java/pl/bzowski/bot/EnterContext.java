package pl.bzowski.bot;

import org.ta4j.core.Indicator;

public class EnterContext {
    private String strategyName;
    private String symbol;
    private StrategyWithIndicators strategy;
    private int enterIndex;

    public EnterContext(String strategyName, String symbol, StrategyWithIndicators strategy, int enterIndex) {
        this.strategyName = strategyName;
        this.symbol = symbol;
        this.strategy = strategy;
        this.enterIndex = enterIndex;
    }

    public boolean isLong() {
        return strategyName.contains("LONG");
    }

    public boolean isShort() {
        return strategyName.contains("SHORT");
    }

    public String getSymbol() {
        return symbol;
    }

    public Indicator getIndicator(Class<? extends Indicator> indicatorClass) {
        return strategy.getIndicator(indicatorClass);
    }

    public int getEnterIndex() {
        return enterIndex;
    }
}
