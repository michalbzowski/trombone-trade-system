package pl.bzowski.bot.strategies;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;

public class StrategiesFactory {

  private static final Logger logger = LoggerFactory.getLogger(StrategiesFactory.class);

  /*
   * Strategy Nameing Convention
   * 1. Prefix
   * 2. Long or Short
   * 3. Indicators
   * 4. Strategy
   * eg SimpleLongSarEma200Strategy
   * or AgressiveShortSarEma200Strategy
   */
  private static Map<Class<? extends StrategyBuilder>, StrategyWithLifeCycle> instances = new HashMap<>();

  public static StrategyWithLifeCycle create(Class<? extends StrategyBuilder> strategyBuilderClass, BarSeries barSeries) {
    if (instances.keySet().contains(strategyBuilderClass)) {
      return instances.get(strategyBuilderClass);
    } else {
      StrategyBuilder newInstance;
      try {
        newInstance = strategyBuilderClass.getConstructor().newInstance();
        StrategyWithLifeCycle strategyWithLifeCycle = newInstance.buildStrategy(barSeries);
        instances.put(strategyBuilderClass, strategyWithLifeCycle);
        return strategyWithLifeCycle;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        logger.error("Crateing strategy instance error", e.getLocalizedMessage());
        return null;
      }
    }
  }
}