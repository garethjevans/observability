package org.garethjevans.observability;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(
    properties = {
      "server.shutdown=immediate",
      "metrics.allow-high-cardinality=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
public class LowCardinalityTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LowCardinalityTest.class);

  @Autowired protected MockMvc mockMvc;

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

    List<String> scrape = Arrays.stream(prometheusResponse.split("\n"))
            .filter(f -> f.startsWith("test_application"))
            .toList();
    assertThat(scrape).hasSize(5);

    scrape.stream().map(s -> "LC> " + s).forEach(LOGGER::debug);

    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("low_key_one");
    assertThat(prometheusResponse).doesNotContain("high_key_one");
  }

  @Test
  public void canGetHighScrape() {
    String prometheusResponse = getHighScrapeEndpoint();
    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("# TYPE application_ready_time_seconds gauge");

    List<String> scrape = Arrays.stream(prometheusResponse.split("\n"))
            .filter(f -> f.startsWith("test_application"))
            .toList();
    assertThat(scrape).hasSizeGreaterThan(10);

    scrape.stream().map(s -> "HC> " + s).forEach(LOGGER::debug);

    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("low_key_one");
    assertThat(prometheusResponse).contains("high_key_one");
  }

  private String getLowScrapeEndpoint() {
    try {
      MvcResult mockRes =
          mockMvc.perform(get("/actuator/prometheus").accept(MediaType.TEXT_PLAIN)).andReturn();
      checkResponseCode(mockRes.getResponse());
      return mockRes.getResponse().getContentAsString();
    } catch (HttpClientErrorException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getHighScrapeEndpoint() {
    try {
      MvcResult mockRes =
              mockMvc.perform(get("/actuator/prometheus-high-cardinality").accept(MediaType.TEXT_PLAIN)).andReturn();
      checkResponseCode(mockRes.getResponse());
      return mockRes.getResponse().getContentAsString();
    } catch (HttpClientErrorException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getActuatorMetricsForTestApplication() {
    try {
      MvcResult mockRes =
              mockMvc.perform(get("/actuator/metrics/test.application").accept(MediaType.APPLICATION_JSON)).andReturn();
      checkResponseCode(mockRes.getResponse());
      return mockRes.getResponse().getContentAsString();
    } catch (HttpClientErrorException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void checkResponseCode(MockHttpServletResponse response)
      throws UnsupportedEncodingException {
    LOGGER.trace(
        "checking response code: {} - {}", response.getStatus(), response.getContentAsString());
    if (!HttpStatus.valueOf(response.getStatus()).is2xxSuccessful()) {
      throw new HttpClientErrorException(
          HttpStatusCode.valueOf(response.getStatus()),
          new String(response.getContentAsByteArray()),
          response.getContentAsByteArray(),
          StandardCharsets.UTF_8);
    }
  }
}
