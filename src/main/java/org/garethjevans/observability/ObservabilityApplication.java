package org.garethjevans.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		CompositeMeterRegistryAutoConfiguration.class
})
public class ObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObservabilityApplication.class, args);
	}

}
