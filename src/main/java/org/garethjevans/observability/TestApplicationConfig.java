package org.garethjevans.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestApplicationConfig {

    @Bean
    public ObservationRegistry observationRegistry(CompositeMeterRegistry compositeMeterRegistry) {
        ObservationRegistry registry = ObservationRegistry
                .create();
        registry.observationConfig()
                .observationHandler(new CustomMeterObservationHandler(compositeMeterRegistry));
        return registry;
    }

    @Bean
    @Qualifier("high")
    public PrometheusMeterRegistry high() {
        PrometheusMeterRegistry high = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        high.config().meterFilter(MeterFilter.accept());
        return high;
    }

    @Bean
    @Qualifier("low")
    public PrometheusMeterRegistry low() {
        PrometheusMeterRegistry low = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        low.config().meterFilter(MeterFilter.ignoreTags("high.key.one", "high.key.two"));
        return low;
    }

    @Bean
    public CompositeMeterRegistry meterRegistry(@Qualifier("high") PrometheusMeterRegistry high,
                                                @Qualifier("low") PrometheusMeterRegistry low) {
        return new CompositeMeterRegistry()
            .add(low)
            .add(high);
    }

    // recreate the metrics endpoint with the low cardinality store
    @Bean
    public MetricsEndpoint metricsEndpoint(@Qualifier("low") PrometheusMeterRegistry registry) {
        return new MetricsEndpoint(registry);
    }

    @Bean
    public ApplicationRunner applicationRunner(ObservationRegistry observationRegistry) {
        return application -> {
            for (int i=0; i<100; i++) {
                var context = new TestApplicationObservationContext();
                context.setLowKeyOne("one");
                context.setLowKeyTwo("two");
                context.setLowKeyThree("three");
                context.setHighKeyOne("one-" + i);
                context.setHighKeyTwo("two-" + i);

                Observation o =
                        TestApplicationObservationDocumentation.TEST_APPLICATION.observation(
                                new TestApplicationObservationConvention(),
                                new TestApplicationObservationConvention(),
                                () -> context,
                                observationRegistry);

                o.observe(() -> {
                    System.out.println("Do Something....");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Done Something....");
                });
            }
        };
    }
}
