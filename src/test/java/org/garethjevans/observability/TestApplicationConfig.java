package org.garethjevans.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestApplicationConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestApplicationConfig.class);

  @Bean
  public ApplicationRunner applicationRunner(ObservationRegistry observationRegistry) {

    return application -> {
      LOGGER.info("Using observation registry: {}", observationRegistry);

      for (int i = 0; i < 100; i++) {
        var context = new ApplicationObservationContext();
        context.setLowKeyOne("one");
        context.setLowKeyTwo("two");
        context.setLowKeyThree("three");
        context.setHighKeyOne("one-" + i);
        context.setHighKeyTwo("two-" + i);

        Observation o =
            ApplicationObservationDocumentation.APPLICATION.observation(
                new ApplicationObservationConvention(),
                new ApplicationObservationConvention(),
                () -> context,
                observationRegistry);

        o.observe(
            () -> {
              try {
                LOGGER.debug("Recording metric... {}", context);
                Thread.sleep(5);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            });
      }
    };
  }
}
