package pl.bzowski.bot.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;

import pl.bzowski.bot.states.PositionClosed;
import pl.bzowski.bot.states.PositionCreated;
import pl.bzowski.bot.states.PositionCreatingFailed;
import pl.bzowski.bot.states.PositionCreatingPending;
import pl.bzowski.bot.states.PositionState;

import java.util.Arrays;

public class StrategyWithLifeCycle extends BaseStrategy {

    private String symbol;
    private PositionState positionState = new PositionClosed();
    private TradingRecord tradingRecord = new BaseTradingRecord();

    private final Indicator[] indicators;
    public Logger logger = LoggerFactory.getLogger(StrategyWithLifeCycle.class);

    public StrategyWithLifeCycle(String name, String symbol, Rule entryRule, Rule exitRule, Indicator... indicators) {
        super(name, entryRule, exitRule);
        this.symbol = symbol;
        this.indicators = indicators;
    }

    @Override
    public boolean shouldEnter(int index) {
        logger.info("Should enter at index {}?", index);
        if (positionState.isOpened()) {
            logger.info("- No - position is already opened {}", positionState.isOpened());
            return false;
        }
        boolean shouldEnter = super.shouldEnter(index);
        logger.info("- Strategy {} should enter: {}. Indicators:", getName(), shouldEnter);
        Arrays.stream(indicators)
                .forEach(i -> logger.debug("Indicator {} - value: {}", i.getClass(), i.getValue(index)));
        return shouldEnter;
    }

    public Indicator getIndicator(Class<? extends Indicator> indicatorClass) {
        return Arrays.stream(indicators).filter(indicator -> indicator.getClass().equals(indicatorClass)).findFirst()
                .orElseThrow();
    }

    public void positionCreatingPending() {
        logger.info("- position pending");
        this.positionState = new PositionCreatingPending();
    }

    public void positionCreatingFailed() {
        logger.info("- position creating failed");
        this.positionState = new PositionCreatingFailed();
    }

    public void positionCreated(long positionId) {
        logger.info("- position created");
        this.positionState = new PositionCreated(positionId);
    }

    public boolean canBeClosed(long positionId) {
        boolean canBeClosed = positionState.canBeClosed(positionId);
        logger.info("- position {} can be closed {}?", positionId, canBeClosed);
        return canBeClosed;
    }

    public void closePosition() {
        logger.info("- position closed now");
        this.positionState = new PositionClosed();
    }

    public long getPositionId() {
        if (positionState.isOpened()) {
            return positionState.getPositionId();
        }
        return 0;
    }

    public boolean isPositionAlreadyOpened() {
        return positionState.isOpened();
    }

    public TradingRecord getTradingRecord() {
      return tradingRecord;
    }
    
    public String getSymbol() {
      return symbol;
    }

    public boolean isLong() {
      return getName().contains("LONG");
    }

    public boolean isShort() {
      return getName().contains("SHORT");
    }
}
