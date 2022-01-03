package pl.bzowski.trader.chart;

import javax.swing.*;

public class SymbolJTextFiled extends JTextField {
    public SymbolJTextFiled() {
        super("AUDUSD", 16);
        setMaximumSize(getPreferredSize());
    }
}
