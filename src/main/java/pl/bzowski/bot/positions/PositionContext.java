package pl.bzowski.bot.positions;

import org.ta4j.core.Indicator;

import pl.bzowski.bot.strategies.StrategyWithLifeCycle;

public class PositionContext {
    private String strategyName;
    private String symbol;
    private StrategyWithLifeCycle strategy;
    private int enterIndex;

    public PositionContext(String symbol, StrategyWithLifeCycle strategy, int enterIndex) {
        this.strategyName = strategy.getName();
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

    public void positionCreatingPending() {
        strategy.positionCreatingPending();
    }

    public void positionCreatingFailed() {
        strategy.positionCreatingFailed();
    }

    public void positionCreated(long positionId) {
        strategy.positionCreated(positionId);
    }

    public boolean canBeClosed(long positionId) {
        return strategy.canBeClosed(positionId);
    }

    public void closePosition() {
        strategy.closePosition();
    }

    public long getPositionId() {
      return strategy.getPositionId();
    }

    public boolean isAlreadyOpened() {
      return strategy.isOpened();
    }
}
