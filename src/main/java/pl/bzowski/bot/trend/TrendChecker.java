package pl.bzowski.bot.trend;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuSenkouSpanAIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuSenkouSpanBIndicator;
import org.ta4j.core.rules.InPipeRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import pl.bzowski.bot.MinuteSeriesHandler;
import pro.xstore.api.message.codes.PERIOD_CODE;

import java.security.InvalidParameterException;

public class TrendChecker {

    private final MinuteSeriesHandler minuteSeriesHandler;

    public TrendChecker(MinuteSeriesHandler minuteSeriesHandler) {
        this.minuteSeriesHandler = minuteSeriesHandler;
    }

    public Trend checkTrend(String symbol, PERIOD_CODE periodCode) {
        BarSeries series = minuteSeriesHandler.convertToPeriod(symbol, periodCode);
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        IchimokuSenkouSpanAIndicator spanA = new IchimokuSenkouSpanAIndicator(series);
        IchimokuSenkouSpanBIndicator spanB = new IchimokuSenkouSpanBIndicator(series);
        int endIndex = series.getEndIndex();
        if (new OverIndicatorRule(closePriceIndicator, spanA)
                .and(new OverIndicatorRule(closePriceIndicator, spanB)).isSatisfied(endIndex)) {
            return new BullishTrend();
        } else if (new InPipeRule(closePriceIndicator, spanA, spanB).isSatisfied(endIndex)) {
            return new NoTrend();
        } else if (new UnderIndicatorRule(closePriceIndicator, spanA)
                .and(new UnderIndicatorRule(closePriceIndicator, spanB)).isSatisfied(endIndex)) {
            return new BearishTrend();
        } else {
            throw new InvalidParameterException("Invalid trend?");
        }
    }
}
