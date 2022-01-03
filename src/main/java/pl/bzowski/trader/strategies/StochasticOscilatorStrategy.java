package pl.bzowski.trader.strategies;

import pl.bzowski.trader.XStationEventHandler;
import pl.bzowski.trader.chart.CandleCounter;
import pl.bzowski.trader.chart.MyCandle;
import pl.bzowski.trader.indicators.IndicatorsFactory;
import pl.bzowski.trader.indicators.StochasticOscilator;
import pl.bzowski.trader.Trader;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.records.STickRecord;
import pro.xstore.api.message.response.APIErrorResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StochasticOscilatorStrategy implements XStationEventHandler {

    //https://comparic.pl/oscylator-stochastyczny-stochastic-dowiedz-sie-krok-kroku-uzywac/
    //Ww fajnie opisuje strategie jak handlować
    private static final double DELTA = 0.005;
    private StrategyState state = new WaitingForASignal();
    private StochasticOscilator stochasticOscilator = IndicatorsFactory.createStochasticOscilator();
    private Trader trader;
    private CandleCounter candleCounter = new CandleCounter();
    private List<RateInfoRecord> candles = new ArrayList<>();
    private UUID openedPositionId;

    public StochasticOscilatorStrategy(Trader trader) {
        this.trader = trader;
    }

    public void testStrategy(String symbol, List<RateInfoRecord> candles) throws APIErrorResponse, APICommunicationException, APIReplyParseException, APICommandConstructionException {
        if (!state.isWaitingForSignal()) {
            return;
        }
        double main = stochasticOscilator.getMain(candles);
        double signal = stochasticOscilator.getSignal(candles);
        //Doczytaj o tym presunieciu
        if (main < 20) {
            state = new BuyState();
            openedPositionId = trader.openBuyPosition(symbol, countStrategyRisk(), this);
        }
        if (main > 80) {
            state = new SellState();
            openedPositionId = trader.openSellPosition(symbol, countStrategyRisk(), this);
        }
    }

    private double countStrategyRisk() {
        return 0.01;//Wartość będzie liczona jako środki_na_koncie * ryzyko_które_chce_podjac - obejzyj filmik Moskwy o moneymanagement, tam opisywał wzór, jak to policzyć
    }

    @Override
    public void handle(STickRecord tickRecord) {
        candleCounter.count(tickRecord, 60_000L, (MyCandle myCandle) -> lol(myCandle));
    }

    private void lol(MyCandle myCandle) {
        candles.add(new RateInfoRecord(myCandle.getTime(), myCandle.getOpen(), myCandle.getHigh(), myCandle.getLow(), myCandle.getClose(), myCandle.getVolume()));
        try {
            testStrategy(myCandle.getSymbol(), candles);
        } catch (APIErrorResponse | APICommunicationException | APIReplyParseException | APICommandConstructionException apiErrorResponse) {
            apiErrorResponse.printStackTrace();
        }
    }

    public void releasePosition() {
        this.state = new WaitingForASignal();
    }
}
