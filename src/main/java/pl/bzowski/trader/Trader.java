package pl.bzowski.trader;

import pl.bzowski.trader.strategies.StochasticOscilatorStrategy;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;

import java.util.UUID;

public interface Trader extends XStationEventHandler {

    UUID openSellPosition(String symbol, double positionVolume, StochasticOscilatorStrategy stochasticOscilatorStrategy) throws APICommandConstructionException, APIErrorResponse, APICommunicationException, APIReplyParseException;

    void closePosition(UUID positionId);

    UUID openBuyPosition(String symbol, double positionVolume, StochasticOscilatorStrategy stochasticOscilatorStrategy) throws APIErrorResponse, APICommunicationException, APIReplyParseException, APICommandConstructionException;
}
