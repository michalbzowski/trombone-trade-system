package pl.bzowski.bot;

import pro.xstore.api.sync.SyncAPIConnector;

public class ClosePosition {
    static void closePosition(SyncAPIConnector connector, EnterContext exitSymbol) {
//        TradesResponse tradesResponse = null;
//        try {
//            tradesResponse = APICommandFactory.executeTradesCommand(connector, true);
//        } catch (APICommandConstructionException | APIReplyParseException | APICommunicationException | APIErrorResponse e) {
//            logger.error(e.getLocalizedMessage());
//        }
//        TradeRecord tradeRecordToClose = null;
//
//        for (TradeRecord tradeRecord : tradesResponse.getTradeRecords()) {
//            if (tradeRecord.getOrder2() == ttsResponseOrderId) {
//                tradeRecordToClose = tradeRecord;
//                double price = tradeRecordToClose.getClose_price();
//                double sl = 0.0;
//                double tp = 0.0;
//                double volume = tradeRecord.getVolume();
//                long order = tradeRecordToClose.getOrder();
//                String customComment = "Close request from bot";
//                long expiration = 0;
//                TradeTransInfoRecord ttCloseInfoRecord = new TradeTransInfoRecord(
//                        TRADE_OPERATION_CODE.BUY,
//                        TRADE_TRANSACTION_TYPE.CLOSE,
//                        price, sl, tp, symbol, volume, order, customComment, expiration);
//                try {
//                    TradeTransactionResponse closeTradeTransactionResponse = APICommandFactory.executeTradeTransactionCommand(connector, ttCloseInfoRecord);
//                    logger.info("Close request for {}, orderId: {}. Status: {}", symbol, closeTradeTransactionResponse.getOrder(), closeTradeTransactionResponse.getStatus());
//                    TradeTransactionStatusResponse ttsCloseResponse = APICommandFactory.executeTradeTransactionStatusCommand(connector, closeTradeTransactionResponse.getOrder());
//                    logger.info("Close response for {}, orderId: {}. Status: {}", symbol, ttsCloseResponse.getOrder(), ttsCloseResponse.getStatus());
//                } catch (APICommandConstructionException | APIReplyParseException | APICommunicationException | APIErrorResponse e) {
//                    logger.error(e.getLocalizedMessage());
//                }
//            }
//        }
    }
}