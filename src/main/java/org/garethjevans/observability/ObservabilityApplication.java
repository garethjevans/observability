package org.garethjevans.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.task.TaskExecutorMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		// CompositeMeterRegistryAutoConfiguration.class,
		PrometheusMetricsExportAutoConfiguration.class,
		// TaskExecutorMetricsAutoConfiguration.class,
})
public class ObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObservabilityApplication.class, args);
	}

}
