package pl.bzowski.commands;

import pl.bzowski.TestContext;
import pl.bzowski.bot.commands.TradeTransactionStatusCommand;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.TradeTransactionStatusResponse;
import pro.xstore.api.sync.SyncAPIConnector;

public class MockedTradeTransactionStatusCommand extends TradeTransactionStatusCommand {

  public MockedTradeTransactionStatusCommand(SyncAPIConnector connector) {
    super(connector);
  }

  @Override
  public TradeTransactionStatusResponse execute(long transactionOrderId)
      throws APICommandConstructionException, APIReplyParseException, APICommunicationException, APIErrorResponse {
    return TestContext.TRADE_TRANSACTION_STATUS_RESPONSE;
  }
  
}
