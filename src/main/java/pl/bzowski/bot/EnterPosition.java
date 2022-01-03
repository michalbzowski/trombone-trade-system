package pl.bzowski.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.num.DecimalNum;
import pro.xstore.api.message.codes.REQUEST_STATUS;
import pro.xstore.api.message.codes.TRADE_OPERATION_CODE;
import pro.xstore.api.message.codes.TRADE_TRANSACTION_TYPE;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.records.TradeRecord;
import pro.xstore.api.message.records.TradeTransInfoRecord;
import pro.xstore.api.message.response.*;
import pro.xstore.api.sync.SyncAPIConnector;

public class EnterPosition {

    private static final Logger logger = LoggerFactory.getLogger(EnterPosition.class);

    static void enterPosition(SyncAPIConnector connector, EnterContext enterContext) {
        try {
            SymbolResponse symbolResponse = APICommandFactory.executeSymbolCommand(connector, enterContext.getSymbol());
            SymbolRecord symbol = symbolResponse.getSymbol();
            boolean isLong = enterContext.isLong();
            double price = isLong ? symbol.getAsk() : symbol.getBid();
            Indicator parabolicSarIndicator = enterContext.getIndicator(ParabolicSarIndicator.class);
            double sl = ((DecimalNum) parabolicSarIndicator.getValue(enterContext.getEnterIndex())).doubleValue();
            double tp = isLong ? price + sl * 1.015 : price - sl * 1.015;
            double volume = symbol.getLotMin();
            long createOrderId = 0;
            String customComment = "Transaction opened by bot";
            long expiration = 0;
            TradeTransInfoRecord ttOpenInfoRecord = new TradeTransInfoRecord(
                    isLong ? TRADE_OPERATION_CODE.BUY : TRADE_OPERATION_CODE.SELL,
                    TRADE_TRANSACTION_TYPE.OPEN,
                    price, sl, tp, enterContext.getSymbol(), volume, createOrderId, customComment, expiration);
            TradeTransactionResponse tradeTransactionResponse = APICommandFactory.executeTradeTransactionCommand(connector, ttOpenInfoRecord);
            long order1 = tradeTransactionResponse.getOrder();
            logger.info("Transaction request for {}. Status {}. Zlecenie (order): {}", symbol, tradeTransactionResponse.getStatus(), order1);
            if (tradeTransactionResponse.getStatus()) {
                TradeTransactionStatusResponse ttsResponse = APICommandFactory.executeTradeTransactionStatusCommand(connector, order1);
                REQUEST_STATUS requestStatus = ttsResponse.getRequestStatus();
                logger.info("Order {} status is: {}.", order1, requestStatus.toString());
                TradesResponse tradesResponse = APICommandFactory.executeTradesCommand(connector, true);
                for (TradeRecord tradeRecord : tradesResponse.getTradeRecords()) {
                    if (tradeRecord.getOrder2() == ttsResponse.getOrder()) {
                        logger.info("Trade position opened at {}. Position number: {} for trade {}", tradeRecord.getOpen_time(), tradeRecord.getPosition(), order1);
                    }
                }
            }
        } catch (APICommandConstructionException | APIReplyParseException | APIErrorResponse | APICommunicationException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}