package pl.bzowski.bot.positions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.xstore.api.message.codes.TRADE_OPERATION_CODE;
import pro.xstore.api.message.codes.TRADE_TRANSACTION_TYPE;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.TradeRecord;
import pro.xstore.api.message.records.TradeTransInfoRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.TradeTransactionResponse;
import pro.xstore.api.message.response.TradeTransactionStatusResponse;
import pro.xstore.api.message.response.TradesResponse;
import pro.xstore.api.sync.SyncAPIConnector;

public class ClosePosition {

    Logger logger = LoggerFactory.getLogger(ClosePosition.class);

    public synchronized long closePosition(SyncAPIConnector connector, PositionContext exitContext) {
        TradesResponse tradesResponse;
        try {
            tradesResponse = APICommandFactory.executeTradesCommand(connector, true);
            TradeRecord tradeRecordToClose = null;
            for (TradeRecord tradeRecord : tradesResponse.getTradeRecords()) {
                if (exitContext.canBeClosed(tradeRecord.getOrder2())) {
                    tradeRecordToClose = tradeRecord;
                }
            }
            if (tradeRecordToClose == null) {
                return 0;// If is not found I consder position was closed by SL / TP
            }
            double price = tradeRecordToClose.getClose_price();
            double sl = 0.0;
            double tp = 0.0;
            String symbol = tradeRecordToClose.getSymbol();
            double volume = tradeRecordToClose.getVolume();
            long order = tradeRecordToClose.getOrder();
            String customComment = "Closed by bot";
            long expiration = 0;
            TradeTransInfoRecord ttCloseInfoRecord = new TradeTransInfoRecord(
                    exitContext.isShort() ? TRADE_OPERATION_CODE.SELL : TRADE_OPERATION_CODE.BUY,
                    TRADE_TRANSACTION_TYPE.CLOSE,
                    price, sl, tp, symbol, volume, order, customComment, expiration);
            TradeTransactionResponse closeTradeTransactionResponse;

            closeTradeTransactionResponse = APICommandFactory.executeTradeTransactionCommand(connector,
                    ttCloseInfoRecord);
            TradeTransactionStatusResponse ttsCloseResponse;
            ttsCloseResponse = APICommandFactory.executeTradeTransactionStatusCommand(connector,
                    closeTradeTransactionResponse.getOrder());
            exitContext.closePosition();
            logger.info("Closed: {}", ttsCloseResponse);
        } catch (APIErrorResponse | APICommandConstructionException | APIReplyParseException
                | APICommunicationException e1) {
            logger.error("Closing position {} failed: {}","xxx", e1);
            return  0;
        }
        return 0;
    }
}