package org.garethjevans.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ObservabilityApplication {

  public static void main(String[] args) {
    SpringApplication.run(ObservabilityApplication.class, args);
  }
}
