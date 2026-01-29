package org.garethjevans.observability;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.Properties;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;

@WebEndpoint(id = "prometheus-all")
public class PrometheusHighCardinalityScrapeEndpoint extends PrometheusScrapeEndpoint {
  public PrometheusHighCardinalityScrapeEndpoint(
      PrometheusRegistry prometheusRegistry, @Nullable Properties exporterProperties) {
    super(prometheusRegistry, exporterProperties);
  }
}
