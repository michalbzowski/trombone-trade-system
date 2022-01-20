package pl.bzowski.trader;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {
    public MyPanel() {
        setPreferredSize(new Dimension(1600, 1050));
        setLayout(new GridLayout(10, 10));
    }
}