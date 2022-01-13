package pl.bzowski.positions;

import org.junit.Test;

import pl.bzowski.TestContext;
import pl.bzowski.bot.commands.SymbolCommand;
import pl.bzowski.bot.commands.TradeTransactionCommand;
import pl.bzowski.bot.commands.TradeTransactionStatusCommand;
import pl.bzowski.bot.commands.TradesCommand;
import pl.bzowski.bot.positions.OpenPosition;
import pl.bzowski.commands.MockedSymbolCommand;
import pl.bzowski.commands.MockedTradeTransactionCommand;
import pl.bzowski.commands.MockedTradeTransactionStatusCommand;
import pl.bzowski.commands.MockedTradesCommand;

public class OpenPositionTest {

  private TradeTransactionCommand tradeTransactionCommand = new MockedTradeTransactionCommand(null);
  private SymbolCommand symbolCommand = new MockedSymbolCommand(null);
  private TradeTransactionStatusCommand tradeTransactionStatusCommand = new MockedTradeTransactionStatusCommand(null);
  private TradesCommand tradesCommand = new MockedTradesCommand(null);
  
  @Test
  public void test() {
    OpenPosition openPosition = new OpenPosition(tradeTransactionCommand, symbolCommand, tradeTransactionStatusCommand, tradesCommand);
    openPosition.openPosition(TestContext.SIMPLE_STRATEGY);
  }
}
