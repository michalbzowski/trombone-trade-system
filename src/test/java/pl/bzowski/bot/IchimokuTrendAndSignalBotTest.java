package pl.bzowski.bot;

import org.junit.Test;
import org.ta4j.core.BarSeries;

import pl.bzowski.TestContext;
import pl.bzowski.TimeMachine;
import pro.xstore.api.message.codes.PERIOD_CODE;

public class IchimokuTrendAndSignalBotTest {

  @Test
  public void lol() {
    TimeMachine timeMachine = new TimeMachine(TestContext.NOW_MOCK);
    PERIOD_CODE candleDurationInMinutes = PERIOD_CODE.PERIOD_M15;
    MinuteSeriesHandler minuteSeriesHandler = new MinuteSeriesHandler();
    String symbol = TestContext.USD_JPY;
    BarSeries usdJpySeries = minuteSeriesHandler.createFourHoursSeries(symbol);
 
    // OpenPosition openPosition = new OpenPosition();
    // ClosePosition closePosition = new ClosePosition();

    // BotInstanceForSymbol bot = new BotInstanceForSymbol(symbol, usdJpySeries, 
    // enterContext -> new OpenPosition().openPosition(TestContext.CONNECTOR_MOCK, enterContext),
    //                 enterContext -> new ClosePosition().closePosition(TestContext.CONNECTOR_MOCK, enterContext));
  }
  
}
