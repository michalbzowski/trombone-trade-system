package pl.bzowski.trader;

import pro.xstore.api.message.records.TickRecord;

import java.util.Collection;

public interface TickRepository {
    Collection<TickRecord> getAll(String symbol);
}
