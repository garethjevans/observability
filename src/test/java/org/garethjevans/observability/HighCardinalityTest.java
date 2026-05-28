package org.garethjevans.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    properties = {"server.shutdown=immediate"},
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles({"test-data", "high-cardinality"})
public class HighCardinalityTest extends AbstractCardinalityTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(HighCardinalityTest.class);

  @Test
  public void canGetMetricsForTestApplication() {
    String metricResponse = getActuatorMetricsForTestApplication();
    assertThat(metricResponse).isNotEmpty();
    assertThat(metricResponse).contains("low.key.one");
    assertThat(metricResponse).doesNotContain("high.key.one");
  }

  @Test
  public void canGetLowScrape() {
    String prometheusResponse = getLowScrapeEndpoint();
    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("# TYPE application_ready_time_seconds gauge");

    List<String> scrape =
        Arrays.stream(prometheusResponse.split("\n"))
            .filter(f -> f.startsWith("test_application"))
            .toList();
    assertThat(scrape).hasSizeGreaterThan(5);
  }

  @Test
  public void canGetHighScrape() {
    String prometheusResponse = getHighScrapeEndpoint();
    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("# TYPE application_ready_time_seconds gauge");

    List<String> scrape =
        Arrays.stream(prometheusResponse.split("\n"))
            .filter(f -> f.startsWith("test_application"))
            .toList();
    assertThat(scrape).hasSizeGreaterThan(10);

    scrape.stream().map(s -> "HC> " + s).forEach(LOGGER::debug);

    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("low_key_one");
    assertThat(prometheusResponse).contains("high_key_one");
  }
}
