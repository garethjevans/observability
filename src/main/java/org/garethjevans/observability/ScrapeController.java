package org.garethjevans.observability;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ScrapeController {

    private final PrometheusMeterRegistry high;
    private final PrometheusMeterRegistry low;

    public ScrapeController(@Qualifier("high") PrometheusMeterRegistry high,
                            @Qualifier("low") PrometheusMeterRegistry low) {
        this.high = high;
        this.low = low;
    }

    @RequestMapping("/high")
    public @ResponseBody String high(@RequestHeader("Accept") String accept) {
        return high.scrape(accept);
    }

    @RequestMapping("/low")
    public @ResponseBody String low(@RequestHeader("Accept") String accept) {
        return low.scrape(accept);
    }
}
