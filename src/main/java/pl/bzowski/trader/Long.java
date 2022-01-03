package pl.bzowski.trader;

public class Long implements PositionDirection {
    @Override
    public boolean isLong() {
        return true;
    }

    @Override
    public boolean isShort() {
        return false;
    }
}
