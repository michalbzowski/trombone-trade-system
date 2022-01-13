package pl.bzowski.commands;

import pl.bzowski.TestContext;
import pl.bzowski.bot.commands.TradeTransactionCommand;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.TradeTransInfoRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.TradeTransactionResponse;
import pro.xstore.api.sync.SyncAPIConnector;

public class MockedTradeTransactionCommand extends TradeTransactionCommand {

  public MockedTradeTransactionCommand(SyncAPIConnector connector) {
    super(connector);
  }

  @Override
  public TradeTransactionResponse execute(TradeTransInfoRecord tr)
      throws APICommandConstructionException, APIReplyParseException, APIErrorResponse, APICommunicationException {
    return TestContext.TRADE_TRANSACTION_RESPONSE;
  }
  
}
