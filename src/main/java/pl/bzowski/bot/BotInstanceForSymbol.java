package pl.bzowski.bot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.awt.Window;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.pnl.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import pl.bzowski.bot.strategies.SimpleLongMACDEma100Strategy;
import pl.bzowski.bot.positions.PositionContext;
import pl.bzowski.bot.strategies.SimpleLongSarEma100Ema150Ema200Strategy;
import pl.bzowski.bot.strategies.SimpleShortMACDEma100Strategy;
import pl.bzowski.bot.strategies.SimpleShortSarEma100Ema150Ema200Strategy;
import pl.bzowski.bot.strategies.StrategyWithLifeCycle;
import pl.bzowski.bot.strategies.SimpleShortSarEma200Strategy;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.SCandleRecord;

import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BotInstanceForSymbol {

    private Logger logger = LoggerFactory.getLogger(BotInstanceForSymbol.class);

    private final BarSeries series;
    private final PERIOD_CODE periodCode;
    private Function<PositionContext, Long> openPosition;
    private Function<PositionContext, Long> closePosition;
    private Set<StrategyWithLifeCycle> strategies = new HashSet<>();

    public BotInstanceForSymbol(String symbol, double spreadRaw, int digits, List<RateInfoRecord> archiveCandles,
            PERIOD_CODE periodCode, Function<PositionContext, Long> openPosition,
            Function<PositionContext, Long> closePosition) {
        this.series = new BaseBarSeries(symbol);
        this.periodCode = periodCode;
        this.openPosition = openPosition;
        this.closePosition = closePosition;
        // Getting the bar series
        double divider = Math.pow(10, digits);
        archiveCandles.forEach(record -> {
            long ctm = record.getCtm();
            long code = periodCode.getCode();
            double close = (record.getOpen() + record.getClose()) / divider;
            double open = record.getOpen() / divider;
            double high = (record.getOpen() + record.getHigh()) / divider;
            double low = (record.getOpen() + record.getLow()) / divider;
            BaseBar bar = getBaseBar(code, ctm, close, open, high, low);
            series.addBar(bar);
        });

        StrategyWithLifeCycle longStrategy = new SimpleLongSarEma100Ema150Ema200Strategy().buildStrategy(series);
        strategies.add(longStrategy);

        StrategyWithLifeCycle shortStrategy = new SimpleShortSarEma100Ema150Ema200Strategy().buildStrategy(series);
        strategies.add(shortStrategy);

        // BACKTEST
        // Running our juicy trading strategy...
        StrategyAnalysis sa = new StrategyAnalysis();
        for (Strategy strategy : strategies) {
            BarSeriesManager manager = new BarSeriesManager(series);
            TradingRecord tradingRecord = manager.run(strategy);
            sa.doIt(series, tradingRecord, strategy, symbol);
            // displayChart(series, strategy, tradingRecord.getPositions());
            // indiChart(symbol, strategy);
        }
    }

    /*
     * We run the strategy
     */
    public void onTick(SCandleRecord record) {
        long ctm = record.getCtm();
        long code = periodCode.getCode();
        BaseBar newBar = getBaseBar(code, ctm, record.getClose(), record.getOpen(), record.getHigh(), record.getLow());
        series.addBar(newBar);

        int endIndex = series.getEndIndex();

        for (StrategyWithLifeCycle strategy : strategies) {
            PositionContext enterContext = new PositionContext(record.getSymbol(), strategy, endIndex);
            loggingDebug(enterContext);
            logger.info("Strategy: {}", strategy.getName());
            boolean shouldEnter = strategy.shouldEnter(endIndex);
            logger.info("- should enter {} on index {}", shouldEnter, endIndex);
            if (shouldEnter) {
                // Our strategy should enter
                logger.info("Strategy {} should ENTER on {}", strategy.getName(), endIndex);
                long id = this.openPosition.apply(enterContext);
                logger.info("Opened position id: {}", id);
            } else if (strategy.shouldExit(endIndex)) {
                long id = closePosition.apply(new PositionContext(record.getSymbol(), strategy, endIndex));
                logger.info("ID after close {}?", id);
            }
        }
    }

    private void loggingDebug(PositionContext enterContext) {
        // logger.debug("Indicators - entering long {}:", enterContext.isLong());
        // logger.debug("Enter index: {}", enterContext.getEnterIndex());
        // logger.debug("EMA: {}",
        // enterContext.getIndicator(EMAIndicator.class).getValue(enterContext.getEnterIndex()));
        // logger.debug("SAR: {}",
        // enterContext.getIndicator(ParabolicSarIndicator.class).getValue(enterContext.getEnterIndex()));
        // logger.debug("CPI: {}",
        // enterContext.getIndicator(ClosePriceIndicator.class).getValue(enterContext.getEnterIndex()));
    }

    private void indiChart(String symbol, Strategy strategy) {
        /*
         * Building chart dataset
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartBarSeries(series,
                ((StrategyWithLifeCycle) strategy).getIndicator(ClosePriceIndicator.class), symbol));
        dataset.addSeries(buildChartBarSeries(series,
                ((StrategyWithLifeCycle) strategy).getIndicator(ParabolicSarIndicator.class), "PSAR"));
        dataset.addSeries(buildChartBarSeries(series,
                ((StrategyWithLifeCycle) strategy).getIndicator(EMAIndicator.class), "EMA200"));

        /*
         * Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Apple Inc. 2013 Close Prices", // title
                "Date", // x-axis label
                "Price Per Unit", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        /*
         * Displaying the chart
         */
        displayChart(chart);
    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Indicators to chart");
        frame.setContentPane(panel);
        frame.pack();
        positionFrameOnScreen(frame, 0.5, 0.5);
        frame.setVisible(true);
    }

    private static org.jfree.data.time.TimeSeries buildChartBarSeries(BarSeries barSeries, Indicator<Num> indicator,
            String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())),
                    indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    private BaseBar getBaseBar(long code, long ctm, double close, double open, double high, double low) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ctm + code * 60_000),
                ZoneId.systemDefault());
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return BaseBar.builder().closePrice(DecimalNum.valueOf(close)).openPrice(DecimalNum.valueOf(open))
                .highPrice(DecimalNum.valueOf(high)).lowPrice(DecimalNum.valueOf(low)).endTime(zonedDateTime)
                .timePeriod(Duration.ofMinutes(code)).build();
    }

    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(BarSeries barSeries, Indicator<Num> indicator,
            String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())),
                    indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    private static void addBuySellSignals(BarSeries series, List<Position> positions, XYPlot plot) {
        // Running the strategy
        // Adding markers to plot
        for (Position position : positions) {
            // Buy signal
            double buySignalBarTime = new Minute(
                    Date.from(series.getBar(position.getEntry().getIndex()).getEndTime().toInstant()))
                            .getFirstMillisecond();
            Marker buyMarker = new ValueMarker(buySignalBarTime);
            buyMarker.setPaint(Color.GREEN);
            buyMarker.setLabel("B");
            plot.addDomainMarker(buyMarker);
            // Sell signal
            double sellSignalBarTime = new Minute(
                    Date.from(series.getBar(position.getExit().getIndex()).getEndTime().toInstant()))
                            .getFirstMillisecond();
            Marker sellMarker = new ValueMarker(sellSignalBarTime);
            sellMarker.setPaint(Color.RED);
            sellMarker.setLabel("S");
            plot.addDomainMarker(sellMarker);
        }
    }

    private static void displayChart(BarSeries series, Strategy strategy, List<Position> positions) {
        /*
         * Building chart datasets
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"));

        /*
         * Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Bitstamp BTC", // title
                "Date", // x-axis label
                "Price", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

        /*
         * Running the strategy and adding the buy and sell signals to plot
         */
        addBuySellSignals(series, positions, plot);

        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Buy and sell signals to chart");
        frame.setContentPane(panel);
        frame.pack();
        positionFrameOnScreen(frame, 0.5, 0.5);
        frame.setVisible(true);
    }

    public static void positionFrameOnScreen(final Window frame, final double horizontalPercent,
            final double verticalPercent) {

        final Rectangle s = frame.getGraphicsConfiguration().getBounds();
        final Dimension f = frame.getSize();
        final int w = Math.max(s.width - f.width, 0);
        final int h = Math.max(s.height - f.height, 0);
        final int x = (int) (horizontalPercent * w) + s.x;
        final int y = (int) (verticalPercent * h) + s.y;
        frame.setBounds(x, y, f.width, f.height);

    }

}
