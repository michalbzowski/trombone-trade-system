package pl.bzowski.commands;

import pl.bzowski.TestContext;
import pl.bzowski.bot.commands.SymbolCommand;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.SymbolResponse;
import pro.xstore.api.sync.SyncAPIConnector;

public class MockedSymbolCommand extends SymbolCommand {

  public MockedSymbolCommand(SyncAPIConnector connector) {
    super(connector);
  }

  @Override
  public SymbolResponse execute(String symbol)
      throws APICommandConstructionException, APIReplyParseException, APIErrorResponse, APICommunicationException {
        return TestContext.SYMBOL_RESPONSE;
  }
  
}
