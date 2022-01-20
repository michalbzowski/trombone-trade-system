package pl.bzowski.trader;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

public class BidClosePriceMinusSpreadIndicator extends ClosePriceIndicator {

    private final double spreadMoreOver;

    public BidClosePriceMinusSpreadIndicator(BarSeries series, double spreadMoreOver) {
        super(series);
        this.spreadMoreOver = spreadMoreOver;
    }

    @Override
    protected Num calculate(int index) {
        final Bar bar = getBarSeries().getBar(index);
        return bar.getClosePrice().min(DecimalNum.valueOf(spreadMoreOver));
    }
}
