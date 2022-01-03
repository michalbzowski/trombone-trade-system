package pl.bzowski.trader.strategies;

public class SellState implements StrategyState {
    @Override
    public boolean isWaitingForSignal() {
        return false;
    }
}
