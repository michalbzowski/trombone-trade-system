package pl.bzowski.trader.indicators;

public class IndicatorsFactory {

    public static StochasticOscilator createStochasticOscilator() {
        return new StochasticOscilator(13, 3, 3);
    }
}
