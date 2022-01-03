package pl.bzowski.trader.balance;

import java.math.BigDecimal;

public class Balance {
    private BigDecimal amount = BigDecimal.valueOf(10_000);

    public Balance() {

    }

    public void minus(double stopLoss) {
        this.amount = amount.subtract(new BigDecimal(stopLoss));
    }

    public void plus(double takeProfit) {
        this.amount = amount.add(new BigDecimal(takeProfit));
    }
}
