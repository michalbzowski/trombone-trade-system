package pl.bzowski.trader.strategies;

public class WaitingForASignal implements StrategyState {
    @Override
    public boolean isWaitingForSignal() {
        return true;
    }
}
