package pl.bzowski;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import pl.bzowski.bot.strategies.StrategyWithLifeCycle;
import pro.xstore.api.message.response.SymbolResponse;
import pro.xstore.api.message.response.TradeTransactionResponse;
import pro.xstore.api.message.response.TradeTransactionStatusResponse;
import pro.xstore.api.message.response.TradesResponse;
import pro.xstore.api.sync.SyncAPIConnector;

public class TestContext {
  public static final String USD_JPY = "USDJPY";
  public static final String EUR_USD = "EURUSD";
  public static final String PLN_USD = "PLNUSD";

  public static final ZonedDateTime NOW_MOCK = ZonedDateTime.of(2020, 12, 1, 12, 15, 0, 0, ZoneId.of("UTC"));
  // public static final SyncAPIConnector CONNECTOR_MOCK = new SyncAPIConnectorMock();
  public static final TradeTransactionResponse TRADE_TRANSACTION_RESPONSE = null;
  public static final SymbolResponse SYMBOL_RESPONSE = null;
  public static final TradeTransactionStatusResponse TRADE_TRANSACTION_STATUS_RESPONSE = null;
  public static final TradesResponse TRADES_RESPONSE = null;
  public static final StrategyWithLifeCycle SIMPLE_STRATEGY = null;
}
