package org.garethjevans.observability;

import io.micrometer.observation.Observation;
import java.util.function.Predicate;

public class HighCardinalityPredicate implements Predicate<Observation.Context> {

  private final MetricsConfigurationProperties config;

  public HighCardinalityPredicate(MetricsConfigurationProperties config) {
    this.config = config;
  }

  @Override
  public boolean test(Observation.Context context) {
    if (config.getHighCardinalityMetrics() != null) {
      for (String metric : config.getHighCardinalityMetrics()) {
        if (context.getName().equals(metric)) {
          return true;
        }
      }
    }
    return false;
  }
}
