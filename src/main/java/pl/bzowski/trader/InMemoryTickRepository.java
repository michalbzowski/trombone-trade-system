package pl.bzowski.trader;

import pro.xstore.api.message.records.TickRecord;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryTickRepository implements TickRepository {
    private final Map<Double, TickRecord> ticks = new LinkedHashMap<>();

    @Override
    public Collection<TickRecord> getAll(String symbol) {
        return ticks.values();
    }
}
