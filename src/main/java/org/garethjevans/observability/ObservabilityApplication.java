package org.garethjevans.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration;

@SpringBootApplication(exclude = {CompositeMeterRegistryAutoConfiguration.class})
@EnableConfigurationProperties
public class ObservabilityApplication {

  public static void main(String[] args) {
    SpringApplication.run(ObservabilityApplication.class, args);
  }
}
