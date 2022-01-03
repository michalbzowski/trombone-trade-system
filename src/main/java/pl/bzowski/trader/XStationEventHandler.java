package pl.bzowski.trader;

import pro.xstore.api.message.records.STickRecord;

public interface XStationEventHandler {
    void handle(STickRecord tickRecord);
}
