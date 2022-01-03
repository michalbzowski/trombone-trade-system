package pl.bzowski.trader.indicators;

import pro.xstore.api.message.records.RateInfoRecord;

import java.util.List;

/**
 * Oscylator stochastyczny (K%D) jest wskaźnikiem momentum używanym w analizie technicznej,
 * wyprowadzony przez George'a Lana w 1950, do porównywania cen zamknięcia do zakresu cen w danym okresie.
 * Wskaźnik STS bazuje na spostrzeżeniu, że podczas trendów wzrostowych ceny zamknięcia kształtują się na ogół
 * blisko górnej granicy swych wahań, zaś w trendach spadkowych zbiżają się do dolnej granicy tego zakresu.
 * W tym oscylatorze używa się dwóch linii – %K i %D.
 * <p>
 * Sposób liczenia wskaźnika.
 * {\displaystyle \%K=100{\frac {cena\ zamkniecia(aktualna)-cena\ minimalna(n)'}{cena\ maksymalna(n)'-cena\ minimalna(n)'}}}{\displaystyle \%K=100{\frac {cena\ zamkniecia(aktualna)-cena\ minimalna(n)'}{cena\ maksymalna(n)'-cena\ minimalna(n)'}}}
 * <p>
 * n – Ilość okresów ' – Cena maksymalna lub minimalna z określonej liczby okresów wstecz
 * <p>
 * %D to 3-okresowa średnia linii %K (tzw. szybki oscylator stochastyczny).
 * <p>
 * Po zastosowaniu tych wzorów powstają dwie linie oscylujące na pionowej skali od 0 do 100. podobnie jak w przypadku RSI wartości skrajne wyznaczane są poziomami 80 i 20. Negatywna dywergencja istnieje wówczas, gdy linia D znajduje się powyżej poziomu 80, tworząc dwa opadające wierzchołki, przy nadal rosnących cenach. Natomiast, dywergencja pozytywna pojawia się w sytuacji, gdy linia D znajduje się poniżej poziomu 20 i kształtuje dwa wznoszące się dołki, przy spadających cenach. Sygnał sprzedaży pojawia się wówczas, gdy szybsza linia %K przecina wolniejszą linię %D powyżej poziomu 80, a sygnał kupna pojawia się wówczas, gdy linia %K przecina linię %D od dołu, tzn. poniżej poziomu 20.
 */
public class StochasticOscilator {

    private static final long MINUTE = 60_000;

    private final int periodK;
    private final int periodD;
    private final int slowing;

    public StochasticOscilator(int periodK, int periodD, int slowing) {
        this.periodK = periodK;
        this.periodD = periodD;
        this.slowing = slowing;
    }

    public double getMain(List<RateInfoRecord> candles) {
        return getMain(candles, 0);
    }

    private double getMain(List<RateInfoRecord> candles, int earlierStart) {
        double actualClosePrice = actualClosePrice(candles);
        double minimumPrice = minimumPrice(candles);
        double maximumPrice = maximumPrice(candles);
        double k = 100 * (actualClosePrice - minimumPrice) / (maximumPrice - minimumPrice);
        return k > 100 || k < 0 ? 50.0 : k;
    }

    private double maximumPrice(List<RateInfoRecord> candles) {
        int size = candles.size();
        double maximumPrice = Double.MIN_VALUE;
        for(int i = size - 1; i >= 0 && i >= size - 1 - periodK; i--) {
            RateInfoRecord rateInfoRecord = candles.get(i);
            double price = rateInfoRecord.getOpen() + rateInfoRecord.getHigh();
            maximumPrice = Double.max(price, maximumPrice);
        }
        return maximumPrice;
    }

    private double minimumPrice(List<RateInfoRecord> candles) {
        int size = candles.size();
        double minimumPrice = Double.MAX_VALUE;
        for(int i = size - 1; i >= 0 && i >= size - 1- periodK; i--) {
            RateInfoRecord rateInfoRecord = candles.get(i);
            double price = rateInfoRecord.getOpen() + rateInfoRecord.getLow();
            minimumPrice = Double.min(price, minimumPrice);
        }
        return minimumPrice;
    }

    private double actualClosePrice(List<RateInfoRecord> candles) {
        if(candles.size() == 0) {
            return 0.0d;
        }
        RateInfoRecord lastCandle = candles.get(candles.size() - 1);
        return lastCandle.getOpen() + lastCandle.getClose();
    }

    public double getSignal(List<RateInfoRecord> candles) {
        double sumForAverage = 0d;
        for(int i = 0; i < periodD; i++) {
            sumForAverage += getMain(candles, i);
        }
        return  sumForAverage / periodD;
    }

    public long getSlowingInMilliseconds() {
        return slowing * MINUTE;
    }
}
