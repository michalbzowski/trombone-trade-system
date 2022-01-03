package pl.bzowski.trader.chart;

import pro.xstore.api.message.codes.PERIOD_CODE;

import javax.swing.*;
import java.util.Arrays;
import java.util.Vector;

public class IntervalComboBox extends JComboBox<PERIOD_CODE> {
    public IntervalComboBox() {
        super(new Vector<>(Arrays.asList(PERIOD_CODE.PERIOD_M1,
                PERIOD_CODE.PERIOD_M5,
                PERIOD_CODE.PERIOD_M15,
                PERIOD_CODE.PERIOD_M30,
                PERIOD_CODE.PERIOD_H1,
                PERIOD_CODE.PERIOD_H4,
                PERIOD_CODE.PERIOD_D1,
                PERIOD_CODE.PERIOD_W1,
                PERIOD_CODE.PERIOD_MN1)));
    }
}
