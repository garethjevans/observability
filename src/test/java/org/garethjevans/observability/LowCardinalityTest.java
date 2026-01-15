package org.garethjevans.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest(
    properties = {"server.shutdown=immediate"},
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles({"test-data"})
public class LowCardinalityTest extends AbstractCardinalityTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LowCardinalityTest.class);

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

    scrape.stream().map(s -> "LC> " + s).forEach(LOGGER::debug);

    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("low_key_one");
    assertThat(prometheusResponse).doesNotContain("high_key_one");
  }

  @Test
  public void canGetHighScrape() {
    try {
      getHighScrapeEndpoint();
      fail("Expected HttpClientErrorException");
    } catch (HttpClientErrorException e) {
      assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
  }
}
