package pl.bzowski.commands;

import pl.bzowski.TestContext;
import pl.bzowski.bot.commands.TradesCommand;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.TradesResponse;
import pro.xstore.api.sync.SyncAPIConnector;

public class MockedTradesCommand extends TradesCommand {

  public MockedTradesCommand(SyncAPIConnector connector) {
    super(connector);
    //TODO Auto-generated constructor stub
  }

  @Override
  public TradesResponse execute(boolean openedOnly)
      throws APICommandConstructionException, APIReplyParseException, APICommunicationException, APIErrorResponse {
    return TestContext.TRADES_RESPONSE;
  }

}
