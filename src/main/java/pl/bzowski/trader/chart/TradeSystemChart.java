package pl.bzowski.trader.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import pl.bzowski.trader.XStationEventHandler;
import pl.bzowski.trader.indicators.IndicatorsFactory;
import pl.bzowski.trader.indicators.StochasticOscilator;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.STickRecord;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class TradeSystemChart extends JFrame implements XStationEventHandler {

    private static final DateFormat READABLE_TIME_FORMAT = new SimpleDateFormat("kk:mm:ss");
    private final StochasticOscilator stochasticOscilator = IndicatorsFactory.createStochasticOscilator();
    private final IntervalComboBox intervalComboBox;

    private OHLCSeries ohlcSeries;
    private TimeSeries volumeSeries;
    private TimeSeries stochSeriesK;
    private TimeSeries stochSeriesD;
    private JFreeChart chart;
    private CandleCounter candleCounter = new CandleCounter();

    public TradeSystemChart(String applicationTitle,
                            String chartTitle,
                            StartTestJButton startTestJButton,
                            SymbolJTextFiled symbolFiled,
                            IntervalComboBox intervalComboBox) {
        super(applicationTitle);
        this.intervalComboBox = intervalComboBox;

        // based on the dataset we create the chart
        // Create new chart
        final JFreeChart candlestickChart = createChart(chartTitle);
        // Create new chart panel
        final ChartPanel chartPanel = new ChartPanel(candlestickChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1600, 1150));
        // Enable zooming
        chartPanel.setMouseZoomable(true);
        chartPanel.setMouseWheelEnabled(true);

        final JPanel controlPanel = createControlPanel(startTestJButton, symbolFiled, intervalComboBox);

        add(controlPanel, BorderLayout.WEST);
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    private JPanel createControlPanel(StartTestJButton startTestJButton, SymbolJTextFiled symbolField, IntervalComboBox intervalComboBox) {
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        placeSymbolLabel(controlPanel);
        placeSymbolField(symbolField, controlPanel);

        placeIntervalLabel(controlPanel);
        controlPanel.add(intervalComboBox);

        JLabel strategy = new JLabel("Strategy");
        strategy.setMaximumSize(strategy.getPreferredSize());
        controlPanel.add(strategy);
        controlPanel.add(new JList<>(new Vector<>(Arrays.asList("SO"))));

        controlPanel.add(new JLabel("Reward : Risk"));
        JTextField rewardRisk = new JTextField("4 : 1", 16);
        rewardRisk.setMaximumSize(rewardRisk.getPreferredSize());
        controlPanel.add(rewardRisk);

        controlPanel.add(new JLabel("Balance Amount Risk (%)"));
        JTextField balanceAmountRisk = new JTextField("2%", 16);
        balanceAmountRisk.setMaximumSize(balanceAmountRisk.getPreferredSize());
        controlPanel.add(balanceAmountRisk);

        controlPanel.add(startTestJButton);
        controlPanel.add(new JButton("AUTO - SCALP"));
        return controlPanel;
    }

    private void placeIntervalLabel(JPanel controlPanel) {
        JLabel interval = new JLabel("Interval");
        interval.setMaximumSize(interval.getPreferredSize());
        controlPanel.add(interval);
    }

    private void placeSymbolField(SymbolJTextFiled symbolField, JPanel controlPanel) {
        controlPanel.add(symbolField);
    }

    private void placeSymbolLabel(JPanel controlPanel) {
        JLabel symbol = new JLabel("Symbol");
        symbol.setMaximumSize(symbol.getPreferredSize());
        controlPanel.add(symbol);
    }

    private JFreeChart createChart(String chartTitle) {

        XYPlot candlestickSubplot = createCandlestickPlot();
        XYPlot volumeSubplot = createVolumeSubplot();

        XYPlot stsSubplot = createStochSubplot();
        /**
         * Create chart main plot with two subplots (candlestickSubplot,
         * volumeSubplot) and one common dateAxis
         */
        // Creating charts common dateAxis
        DateAxis dateAxis = new DateAxis("Time");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("kk:mm"));
        // reduce the default left/right margin from 0.05 to 0.02
        dateAxis.setLowerMargin(0.02);
        dateAxis.setUpperMargin(0.02);
        // Create mainPlot
        CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
        mainPlot.setGap(10.0);
        mainPlot.add(candlestickSubplot, 3);
        mainPlot.add(volumeSubplot, 1);
        mainPlot.add(stsSubplot, 1);
        mainPlot.setOrientation(PlotOrientation.VERTICAL);

        this.chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
        this.chart.removeLegend();

        return chart;
    }

    private XYPlot createStochSubplot() {
        /**
         * Creating stoch subplot
         */
        TimeSeriesCollection stochDataset = new TimeSeriesCollection();
        this.stochSeriesK = new TimeSeries("Stoch %K");
        this.stochSeriesD = new TimeSeries("Stoch %D");
        stochDataset.addSeries(stochSeriesK);
        stochDataset.addSeries(stochSeriesD);
        // Create stoch chart volumeAxis
        NumberAxis stsAxis = new NumberAxis("STS");
        stsAxis.setAutoRangeIncludesZero(false);
        // Set to no decimal
        stsAxis.setNumberFormatOverride(new DecimalFormat("0"));
        //Create sts chart tenderer
        XYSplineRenderer stsRenderer = new XYSplineRenderer();
        stsRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("STS--> Time={1} Size={2}",
                new SimpleDateFormat("kk:mm"), new DecimalFormat("0")));

        stsRenderer.setSeriesPaint(0, Color.RED);
        stsRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        stsRenderer.setSeriesShapesVisible(0, false);

        stsRenderer.setSeriesPaint(1, Color.BLUE);
        stsRenderer.setSeriesStroke(1, new BasicStroke(2.0f));
        stsRenderer.setSeriesShapesVisible(1, false);
        //Create stsSubplot
        XYPlot stsSubplot = new XYPlot(stochDataset, null, stsAxis, stsRenderer);
        stsSubplot.setBackgroundPaint(Color.black);
        return stsSubplot;
    }

    private XYPlot createVolumeSubplot() {
        /**
         * Creating volume subplot
         */
        // creates TimeSeriesCollection as a volume dataset for volume chart
        TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
        volumeSeries = new TimeSeries("Volume");
        volumeDataset.addSeries(volumeSeries);
        // Create volume chart volumeAxis
        NumberAxis volumeAxis = new NumberAxis("Volume");
        volumeAxis.setAutoRangeIncludesZero(false);
        // Set to no decimal
        volumeAxis.setNumberFormatOverride(new DecimalFormat("0"));
        // Create volume chart renderer
        XYBarRenderer timeRenderer = new XYBarRenderer();
        timeRenderer.setShadowVisible(false);
        timeRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Volume--> Time={1} Size={2}",
                new SimpleDateFormat("kk:mm"), new DecimalFormat("0")));
        // Create volumeSubplot
        XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
        volumeSubplot.setBackgroundPaint(Color.black);
        return volumeSubplot;
    }

    private XYPlot createCandlestickPlot() {
        /**
         * Creating candlestick subplot
         */
        // Create OHLCSeriesCollection as a price dataset for candlestick chart
        OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
        ohlcSeries = new OHLCSeries("Price");
        candlestickDataset.addSeries(ohlcSeries);
        // Create candlestick chart priceAxis
        NumberAxis priceAxis = new NumberAxis("Price");
        priceAxis.setAutoRangeIncludesZero(false);
        // Create candlestick chart renderer
        CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE,
                false, new CustomHighLowItemLabelGenerator(new SimpleDateFormat("kk:mm"), new DecimalFormat("0.000")));
        // Create candlestickSubplot
        XYPlot candlestickSubplot = new XYPlot(candlestickDataset, null, priceAxis, candlestickRenderer);
        candlestickSubplot.setBackgroundPaint(Color.black);
        return candlestickSubplot;
    }

    List<RateInfoRecord> candles = new ArrayList<>();

    /**
     * Fill series with data.
     */
    public void addCandle(MyCandle myCandle) {
        try {
            // Add bar to the data. Let's repeat the same bar
            FixedMillisecond t = new FixedMillisecond(
                    READABLE_TIME_FORMAT.parse(TimeUtils.convertToReadableTime(myCandle.getTime())));
            FixedMillisecond slowing = new FixedMillisecond(
                    READABLE_TIME_FORMAT.parse(TimeUtils.convertToReadableTime(myCandle.getTime() - stochasticOscilator.getSlowingInMilliseconds())));

            ohlcSeries.add(t, myCandle.getOpen(), myCandle.getHigh(), myCandle.getLow(), myCandle.getClose());
            volumeSeries.add(t, myCandle.getVolume());
            candles.add(new RateInfoRecord(myCandle.getTime(), myCandle.getOpen(), myCandle.getHigh(), myCandle.getLow(), myCandle.getClose(), myCandle.getVolume()));
            stochSeriesK.add(t, stochasticOscilator.getMain(candles));
//            stochSeriesD.add(slowing, stochasticOscilator.getSignal(candles));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    long MIN_IN_MILISCONDS = 60_000;

    @Override
    public void handle(STickRecord tickRecord) {
        long INTERVAL = ((PERIOD_CODE) intervalComboBox.getSelectedItem()).getCode() * MIN_IN_MILISCONDS;
        candleCounter.count(tickRecord, INTERVAL, (MyCandle myCandle) -> addCandle(myCandle));
    }
}
