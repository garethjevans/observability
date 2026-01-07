package org.garethjevans.observability;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LowCardinalityScrapeController {

    private final PrometheusMeterRegistry low;

    public LowCardinalityScrapeController(@Qualifier("low") PrometheusMeterRegistry low) {
        this.low = low;
    }

    @RequestMapping("/actuator/prometheus")
    public @ResponseBody String low(@RequestHeader("Accept") String accept) {
        return low.scrape(accept);
    }
}
