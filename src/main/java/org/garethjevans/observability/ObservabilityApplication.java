package org.garethjevans.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		//PrometheusMetricsExportAutoConfiguration.class,
})
public class ObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObservabilityApplication.class, args);
	}

}
