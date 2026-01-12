package org.garethjevans.observability.controllers;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ConditionalOnBooleanProperty(name="metrics.allow-high-cardinality", havingValue = true)
public class HighCardinalityScrapeController {

    private final PrometheusMeterRegistry high;

    public HighCardinalityScrapeController(@Qualifier("high") PrometheusMeterRegistry high) {
        this.high = high;
    }

    @RequestMapping("/actuator/prometheus-high-cardinality")
    public @ResponseBody String high(@RequestHeader("Accept") String accept) {
        return high.scrape(accept);
    }

}
