package pl.bzowski.trader;

import pl.bzowski.trader.chart.IntervalComboBox;
import pl.bzowski.trader.chart.SymbolJTextFiled;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.ChartRangeInfoRecord;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.ChartResponse;
import pro.xstore.api.sync.SyncAPIConnector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class StartTestAction implements ActionListener {
    private static final long ONE_DAY = 86_400_000;
    private static final long TWO_HOURS = 86_400_000 / 12;

    private final SyncAPIConnector connector;
    private XStationStreamingListener xStationStreamingListener;
    private final SymbolJTextFiled symbolField;
    private IntervalComboBox intervalComboBox;

    public StartTestAction(SyncAPIConnector connector, XStationStreamingListener xStationStreamingListener, SymbolJTextFiled symbolField, IntervalComboBox intervalComboBox) {
        this.connector = connector;
        this.xStationStreamingListener = xStationStreamingListener;
        this.symbolField = symbolField;
        this.intervalComboBox = intervalComboBox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long NOW = System.currentTimeMillis();
        String symbol = symbolField.getText();
        PERIOD_CODE selectedItem = (PERIOD_CODE) intervalComboBox.getSelectedItem();
        ChartRangeInfoRecord record = new ChartRangeInfoRecord(symbol, selectedItem, NOW - TWO_HOURS, NOW);
        try {
            ChartResponse chartResponse = APICommandFactory.executeChartRangeCommand(connector, record);
            List<RateInfoRecord> rateInfos = chartResponse.getRateInfos().stream().map(r -> r.shiftLeft(chartResponse.getDigits())).collect(Collectors.toList());
            for (int i = 0; i < rateInfos.size(); i++) {
                //W trakcie testów zmieniam archwialną świecę na serię ticków
                RateInfoRecord currentCandle = rateInfos.get(i);
                List<RateInfoRecord> currentCandleWithPastOnes = rateInfos.subList(0, i);
                CandleToTicksConverter candleToTicksConverter = new CandleToTicksConverter(currentCandle, selectedItem, symbol);
                candleToTicksConverter.tickStream(tick -> xStationStreamingListener.receiveTickRecord(tick));
            }
        } catch (APICommandConstructionException | APIReplyParseException | APIErrorResponse | APICommunicationException exception) {
            exception.printStackTrace();
        }
    }
}
