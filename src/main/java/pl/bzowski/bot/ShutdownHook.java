package pl.bzowski.bot;

import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.sync.SyncAPIConnector;

import java.util.Map;

public class ShutdownHook {
    public static void shutdownHook(SyncAPIConnector connector, Map<String, IchimokuTrendAndSignalBot> buttons) {
        Thread printingHook = new Thread(() -> {
            try {
                buttons.keySet().forEach(symbol -> {
                    try {
                        connector.unsubscribeCandle(symbol);
                        connector.disconnectStream();
                    } catch (APICommunicationException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                connector.disconnectStream();
            }
        });
        Runtime.getRuntime().addShutdownHook(printingHook);
    }
}