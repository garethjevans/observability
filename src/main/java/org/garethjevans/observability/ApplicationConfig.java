package org.garethjevans.observability;

import static io.micrometer.core.instrument.config.MeterFilter.forMeters;
import static io.micrometer.core.instrument.config.MeterFilter.ignoreTags;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

  @Bean
  @Qualifier("lowCardinalityRegistry")
  PrometheusMeterRegistry lowCardinalityRegistry(MetricsConfigurationProperties config) {
    PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    for (TagFilter filter : config.getHighCardinalityTagFilters()) {
      registry
          .config()
          .meterFilter(
              forMeters(
                  c -> isCustomHighCardinalityMeter(c, config), ignoreTags(filter.getTagName())));
    }
    return registry;
  }

  @Bean
  @Qualifier("highCardinalityRegistry")
  @ConditionalOnBooleanProperty(name = "metrics.allow-high-cardinality", havingValue = true)
  PrometheusMeterRegistry highCardinalityRegistry() {
    return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  }

  // @Qualifier is only needed here because the name of the parameter is registry and not
  // lowCardinalityRegistry (the name of the bean).
  // If the name of the parameter would be lowCardinalityRegistry, @Qualifier would not be needed,
  // see the next method.
  @Bean
  PrometheusScrapeEndpoint prometheusEndpoint(
      @Qualifier("lowCardinalityRegistry") PrometheusMeterRegistry registry,
      PrometheusConfig prometheusConfig) {
    return new PrometheusScrapeEndpoint(
        registry.getPrometheusRegistry(), prometheusConfig.prometheusProperties());
  }

  @Bean
  @ConditionalOnBooleanProperty(name = "metrics.allow-high-cardinality", havingValue = true)
  PrometheusHighCardinalityScrapeEndpoint prometheusHighCardinalityScrapeEndpoint(
      @Qualifier("highCardinalityRegistry") PrometheusMeterRegistry highCardinalityRegistry,
      PrometheusConfig prometheusConfig) {
    return new PrometheusHighCardinalityScrapeEndpoint(
        highCardinalityRegistry.getPrometheusRegistry(), prometheusConfig.prometheusProperties());
  }

  @Bean
  HighCardinalityMeterObservationHandler highCardinalityMeterObservationHandler(
      MeterRegistry registry, MetricsConfigurationProperties config) {
    return new HighCardinalityMeterObservationHandler(
        registry, new HighCardinalityPredicate(config));
  }

  private boolean isCustomHighCardinalityMeter(Meter.Id id, MetricsConfigurationProperties config) {
    if (config.getHighCardinalityMetrics() != null) {
      for (String metric : config.getHighCardinalityMetrics()) {
        if (id.getName().equals(metric) || id.getName().startsWith(metric + ".")) {
          return true;
        }
      }
    }
    return false;
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
