package pl.bzowski.bot.positions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
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

public class OpenPosition {

    private final Logger logger = LoggerFactory.getLogger(OpenPosition.class);

    public synchronized long openPosition(SyncAPIConnector connector, PositionContext enterContext) {
        if(enterContext.isAlreadyOpened()) {
            logger.info("Position is already opened - skiping");
            return enterContext.getPositionId();
        }
        try {
            enterContext.positionCreatingPending();
            SymbolRecord symbol = getSymbolRecordFromBroker(connector, enterContext);
            TradeTransInfoRecord tradeRequest = prepareTradeRequest(enterContext, symbol);
            TradeTransactionResponse tradeTransactionResponse = APICommandFactory.executeTradeTransactionCommand(connector, tradeRequest);
            long transactionOrderId = tradeTransactionResponse.getOrder();
            logger.info("Transaction request for {}. Status {}. OrderId: {}", symbol, tradeTransactionResponse.getStatus(), transactionOrderId);
            if (tradeTransactionResponse.getStatus()) {
                TradeTransactionStatusResponse statusResponse = APICommandFactory.executeTradeTransactionStatusCommand(connector, transactionOrderId);
                REQUEST_STATUS tradeStatus = statusResponse.getRequestStatus();
                logger.info("Order {} status is: {}. (3 - accepted)", transactionOrderId, tradeStatus.toString());
                TradesResponse tradesResponse = APICommandFactory.executeTradesCommand(connector, true);
                for (TradeRecord tradeRecord : tradesResponse.getTradeRecords()) {
                    if (tradeRecord.getOrder2() == statusResponse.getOrder()) {
                        logger.info("Trade position opened at {}. Position number: {}", tradeRecord.getOpen_time(), tradeRecord.getOrder2());
                        enterContext.positionCreated(tradeRecord.getOrder2());
                        return tradeRecord.getOrder2();
                    }
                }
            }
        } catch (APICommandConstructionException | APIReplyParseException | APIErrorResponse | APICommunicationException e) {
            logger.error(e.getLocalizedMessage());
            enterContext.positionCreatingFailed();
            return 0;
        }
        logger.error("Open position error - Transaction request not true or trade not found");
        enterContext.positionCreatingFailed();
        return 0;
    }

    private TradeTransInfoRecord prepareTradeRequest(PositionContext enterContext, SymbolRecord symbol) {
        boolean isLong = enterContext.isLong();
        double price = isLong ? symbol.getAsk() : symbol.getBid();   
        double sl = 0;//Na razie dam otwierac i zamykac botowi :)
        double tp = 0;
        double volume = symbol.getLotMin();
        long createOrderId = 0;
        String customComment = "Transaction opened by bot";
        long expiration = 0;
        TradeTransInfoRecord ttOpenInfoRecord = new TradeTransInfoRecord(
                isLong ? TRADE_OPERATION_CODE.BUY : TRADE_OPERATION_CODE.SELL,
                TRADE_TRANSACTION_TYPE.OPEN,
                price, sl, tp, enterContext.getSymbol(), volume, createOrderId, customComment, expiration);
        return ttOpenInfoRecord;
    }

    private SymbolRecord getSymbolRecordFromBroker(SyncAPIConnector connector, PositionContext enterContext)
            throws APICommandConstructionException, APIReplyParseException, APIErrorResponse,
            APICommunicationException {
        SymbolResponse symbolResponse = APICommandFactory.executeSymbolCommand(connector, enterContext.getSymbol());
        SymbolRecord symbol = symbolResponse.getSymbol();
        return symbol;
    }
}