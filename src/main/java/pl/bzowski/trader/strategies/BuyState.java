package pl.bzowski.trader.strategies;

public class BuyState implements StrategyState {
    @Override
    public boolean isWaitingForSignal() {
        return false;
    }
}
