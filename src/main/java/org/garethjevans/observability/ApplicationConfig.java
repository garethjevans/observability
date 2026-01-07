package org.garethjevans.observability;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Predicate;

@Configuration
public class ApplicationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    @Qualifier("high")
    @ConditionalOnBooleanProperty(name="metrics.allow-high-cardinality", havingValue = true)
    public PrometheusMeterRegistry high() {
        PrometheusMeterRegistry high = new PrometheusMeterRegistry(new HighCardinalityPrometheusConfig());
        high.config()
                .meterFilter(MeterFilter.acceptNameStartsWith("test.application"))
                .meterFilter(MeterFilter.maximumAllowableTags(
                    new ApplicationObservationConvention().getName(),
                        ApplicationObservationDocumentation.HighCardinalityKeyNames.ONE.asString(),
                        50,
                        logAndDeny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                    new ApplicationObservationConvention().getName(),
                        ApplicationObservationDocumentation.HighCardinalityKeyNames.TWO.asString(),
                        50,
                        logAndDeny()));
        return high;
    }

    @Bean
    @Qualifier("low")
    public PrometheusMeterRegistry low(@Value("${metrics.allow-high-cardinality:false}") boolean allowHighCardinality) {
        PrometheusMeterRegistry low = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        if (allowHighCardinality) {
            low.config()
                    .meterFilter(MeterFilter.denyNameStartsWith("test.application"));
        }

        low.config().meterFilter(MeterFilter.ignoreTags(
                ApplicationObservationDocumentation.HighCardinalityKeyNames.ONE.asString(),
                ApplicationObservationDocumentation.HighCardinalityKeyNames.TWO.asString()));

        return low;
    }

    @Bean
    public ObservationRegistry observationRegistry(CompositeMeterRegistry meterRegistry) {
        meterRegistry
                .getRegistries()
                .forEach(registry -> { LOGGER.info("Registry -  {}", registry); });

        ObservationRegistry registry = ObservationRegistry
                .create();

        registry.observationConfig()
                .observationHandler(new CustomMeterObservationHandler(meterRegistry));

        return registry;
    }

    // recreate the metrics endpoint with the low cardinality store
    @Bean
    public MetricsEndpoint metricsEndpoint(@Qualifier("low") PrometheusMeterRegistry registry) {
        return new MetricsEndpoint(registry);
    }

    public static class HighCardinalityPrometheusConfig implements PrometheusConfig {

        @Override
        public String prefix() {
            return "prometheus-high-cardinality";
        }

        @Override
        public String get(String key) {
            return null;
        }

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
