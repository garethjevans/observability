package org.garethjevans.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestApplicationConfig {

    @Bean
    public ApplicationRunner applicationRunner(ObservationRegistry observationRegistry) {
        return application -> {
            var context = new TestApplicationObservationContext();
            context.setLowKeyOne("one");
            context.setLowKeyTwo("two");
            context.setLowKeyThree("three");
            context.setHighKeyOne("one");
            context.setHighKeyTwo("two");

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
        };
    }
}
