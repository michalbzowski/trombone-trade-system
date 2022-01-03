package pl.bzowski.trader.chart;

public class EmptyTickRecord extends pro.xstore.api.message.records.TickRecord {


    public EmptyTickRecord() {
        this.low = 0d;
        this.high = 0d;
    }

}
