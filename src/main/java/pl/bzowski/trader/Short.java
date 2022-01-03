package pl.bzowski.trader;

public class Short implements PositionDirection {
    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public boolean isShort() {
        return true;
    }
}
