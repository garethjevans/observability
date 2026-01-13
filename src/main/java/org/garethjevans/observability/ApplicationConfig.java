package org.garethjevans.observability;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ApplicationConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

  @Bean
  @Qualifier("high")
  @ConditionalOnBooleanProperty(name = "metrics.allow-high-cardinality", havingValue = true)
  public PrometheusMeterRegistry high(TagFilters tagFilters) {
    PrometheusMeterRegistry high = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    if (tagFilters != null && tagFilters.getFilters() != null) {
      for (TagFilter filter : tagFilters.getFilters()) {
        // only add those filters with non-negative values
        if (filter.getMaxValues() >= 0) {
          high.config()
              .meterFilter(
                  MeterFilter.maximumAllowableTags(
                      filter.getMetricName(),
                      filter.getTagName(),
                      filter.getMaxValues(),
                      logAndDeny()));
        }
      }
    }
    return high;
  }

  @Bean
  @Qualifier("low")
  public PrometheusMeterRegistry low() {
    return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  }

  @Bean
  public ObservationRegistry observationRegistry(
      CompositeMeterRegistry meterRegistry,
      @Value("${metrics.allow-high-cardinality:false}") boolean allowHighCardinality) {
    meterRegistry
        .getRegistries()
        .forEach(
            registry -> {
              LOGGER.info("Registry -  {}", registry);
            });
    ObservationRegistry registry = ObservationRegistry.create();

    LOGGER.info("Observation Registry - created {}", meterRegistry);

    registry
        .observationConfig()
        .observationHandler(new CustomMeterObservationHandler(meterRegistry, allowHighCardinality));

    return registry;
  }

  @Bean
  @Primary
  public CompositeMeterRegistry compositeMeterRegistry(
      List<PrometheusMeterRegistry> meterRegistries) {
    CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
    for (PrometheusMeterRegistry meterRegistry : meterRegistries) {
      LOGGER.info("Adding PrometheusMeterRegistry {} to CompositeMeterRegistry", meterRegistry);
      compositeMeterRegistry.add(meterRegistry);
    }
    return compositeMeterRegistry;
  }

  // recreate the metrics endpoint with the low cardinality store
  @Bean
  public MetricsEndpoint metricsEndpoint(@Qualifier("low") PrometheusMeterRegistry registry) {
    return new MetricsEndpoint(registry);
  }

  static MeterFilter logAndDeny(Predicate<Meter.Id> iff) {
    return new MeterFilter() {
      @Override
      public MeterFilterReply accept(Meter.Id id) {
        if (iff.test(id)) {
          LOGGER.warn("Dropping meter for id {}", id);
          return MeterFilterReply.DENY;
        }
        return MeterFilterReply.NEUTRAL;
      }
    };
  }

  static MeterFilter logAndDeny() {
    return logAndDeny(id -> true);
  }
}
