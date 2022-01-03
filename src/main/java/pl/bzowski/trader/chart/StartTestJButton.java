package pl.bzowski.trader.chart;

import javax.swing.*;
import java.awt.event.ActionListener;

public class StartTestJButton extends JButton {

    public StartTestJButton(ActionListener startTestActionListener) {
        super("TEST");
        addActionListener(startTestActionListener);
    }
}
