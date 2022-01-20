package pl.bzowski.trader;

import pl.bzowski.trader.balance.Balance;
import pl.bzowski.trader.strategies.StochasticOscilatorStrategy;

public class MyPosition {

    private final String symbol;
    private final PositionDirection positionDirection;
    private final double price;
    private final double stopLoss;
    private final double takeProfit;
    private final StochasticOscilatorStrategy stochasticOscilatorStrategy;
    private PositionState positionState = new PositionOpened();

    public MyPosition(String symbol, PositionDirection positionDirection, double price, double stopLoss, double takeProfit, StochasticOscilatorStrategy stochasticOscilatorStrategy) {
        this.symbol = symbol;
        this.positionDirection = positionDirection;
        this.price = price;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.stochasticOscilatorStrategy = stochasticOscilatorStrategy;
    }

    public void closePosition() {
        this.positionState = new PositionClosed();
        stochasticOscilatorStrategy.releasePosition();
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public double getTakeProfit() {
        return takeProfit;
    }

    /*
    Podczas inwestowania na rynku finansowym widzisz zarówno cenę sprzedaży, jak i cenę kupna.
    Cena kupna jest zawsze wyższa od ceny sprzedaży, a różnica między tymi dwiema cenami nazywana jest spreadem,
    który jednocześnie stanowi koszt otwarcia  transakcji na rynku.

    Cena ask oznacza cenę, po której inwestor może zawrzeć transakcję kupna.
    Inaczej, cenę ask można zdefiniować jako cenę, po której jesteś w stanie kupić instrument finansowy.
    Należy również pamietać, że
    przy otwieraniu pozycji długiej, transakcja
        zostanie otwarta po cenie kupna (ask),
        a zamknięta po cenie sprzedaży (bid).

    Z drugiej strony, krótką pozycję
        otworzy się po cenie sprzedaży (bid),
        a zamknie po cenie kupna (ask).


    Pozycja Long

    ----ASK

     */
    public void checkPosition(double ask, double bid, Balance balance) {
        if (positionDirection.isLong() && bid <= stopLoss) {
            closePosition();
            balance.minus(stopLoss);
        }
        if (positionDirection.isShort() && ask >= stopLoss) {
            closePosition();
            balance.minus(stopLoss);
        }
        if (positionDirection.isLong() && bid >= takeProfit) {
            closePosition();
            balance.plus(takeProfit);
        }
        if (positionDirection.isShort() && ask <= takeProfit) {
            closePosition();
            balance.plus(takeProfit);
        }
    }
}
