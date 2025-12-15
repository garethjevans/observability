package org.garethjevans.observability;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    @Qualifier("high")
    public PrometheusMeterRegistry high() {
        PrometheusMeterRegistry high = new PrometheusMeterRegistry(new HighCardinalityPrometheusConfig());
        high.config().meterFilter(MeterFilter.accept());
        return high;
    }

    @Bean
    @Qualifier("low")
    public PrometheusMeterRegistry low() {
        PrometheusMeterRegistry low = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        low.config().meterFilter(MeterFilter.ignoreTags(
                ApplicationObservationDocumentation.HighCardinalityKeyNames.ONE.asString(),
                ApplicationObservationDocumentation.HighCardinalityKeyNames.TWO.asString()));
        return low;
    }

//    @Bean
//    public CompositeMeterRegistry meterRegistry(@Qualifier("high") PrometheusMeterRegistry high,
//                                       @Qualifier("low") PrometheusMeterRegistry low) {
//        return new CompositeMeterRegistry()
//            .add(low)
//            .add(high);
//    }

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
}
