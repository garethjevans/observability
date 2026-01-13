package org.garethjevans.observability;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
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
    // Create a separate PrometheusRegistry for high cardinality metrics
    PrometheusRegistry prometheusRegistry = new PrometheusRegistry();
    PrometheusMeterRegistry high = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, prometheusRegistry, Clock.SYSTEM);

    if (tagFilters != null && tagFilters.getFilters() != null) {
      for (TagFilter filter : tagFilters.getFilters()) {
        // only add those filters with non-negative values
        if (filter.getMaxValues() > 0) {
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
  public PrometheusMeterRegistry low(TagFilters tagFilters) {
    // Create a separate PrometheusRegistry for low cardinality metrics
    PrometheusRegistry prometheusRegistry = new PrometheusRegistry();
    PrometheusMeterRegistry low = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, prometheusRegistry, Clock.SYSTEM);

    if (tagFilters != null && tagFilters.getFilters() != null) {
      for (TagFilter filter : tagFilters.getFilters()) {
        // TODO should we maintain a list of already added filters?
        low.config().meterFilter(MeterFilter.ignoreTags(filter.getTagName()));
      }
    }
    return low;
  }

  @Bean
  public ObservationRegistry observationRegistry(
      @Qualifier("low") PrometheusMeterRegistry lowRegistry,
      @Qualifier("high") java.util.Optional<PrometheusMeterRegistry> highRegistry,
      @Value("${metrics.allow-high-cardinality:false}") boolean allowHighCardinality) {
    ObservationRegistry registry = ObservationRegistry.create();

    LOGGER.info("Observation Registry - created with low registry and high registry (if enabled)");

    // Always add the low cardinality handler (no prefix)
    registry
        .observationConfig()
        .observationHandler(new CustomMeterObservationHandler(lowRegistry));

    // If high cardinality is enabled, add a separate handler with "high." prefix
    if (allowHighCardinality && highRegistry.isPresent()) {
      LOGGER.info("Adding high cardinality observation handler with 'high.' prefix");
      registry
          .observationConfig()
          .observationHandler(new HighCardinalityMeterObservationHandler(highRegistry.get()));
    }

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
