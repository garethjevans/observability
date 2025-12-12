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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(
    properties = {
      "server.shutdown=immediate",
      "mcp.proxy.routes.github.route-path=/github",
      "mcp.proxy.routes.github.target-mcp-server-url=https://github-gwd-mcp-server.apps.tanzu.broadcom.net",
      "mcp.proxy.routes.github.oauth.authorization-server-url=http://localhost:9000",
      "mcp.proxy.routes.github.oauth.name=GitHub Auth Server",
      "mcp.proxy.routes.github.oauth.scope=message.read",
      "mcp.proxy.routes.github.oauth.identifier=http://localhost:9000/github",
      "mcp.proxy.routes.github.introspection.introspection-uri=http://localhost:9000/introspection",
      "mcp.proxy.routes.github.introspection.client-id=client_id",
      "mcp.proxy.routes.github.introspection.client-secret=client_secret"
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
public class ActuatorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorTest.class);

  @Autowired protected MockMvc mockMvc;

  @Test
  public void canAccessActuatorEndpoints() {
    String actuatorResult = getActuator();
    assertThat(actuatorResult).isNotNull();

    actuatorResult = getActuatorHealth();

    assertThat(actuatorResult).isNotNull();
    assertThat(actuatorResult).contains("{\"status\":\"UP\"}");
  }

  @Test
  public void canGetMetrics() {
    String metricResponse = getActuatorMetrics();
    assertThat(metricResponse).isNotEmpty();
    assertThat(metricResponse).contains("application.ready.time");
  }

  @Test
  public void canGetMetricsForTestApplication() {
    String metricResponse = getActuatorMetricsForTestApplication();
    assertThat(metricResponse).isNotEmpty();
    assertThat(metricResponse).contains("low.key.one");
    assertThat(metricResponse).doesNotContain("high.key.one");
  }

  @Test
  public void canGetPrometheus() {
    String prometheusResponse = getActuatorPrometheus();
    assertThat(prometheusResponse).isNotEmpty();
    assertThat(prometheusResponse).contains("# TYPE application_ready_time_seconds gauge");
  }

  private String getActuator() {
    try {
      MvcResult mockRes = mockMvc.perform(get("/actuator")).andReturn();
      checkResponseCode(mockRes.getResponse());
      return mockRes.getResponse().getContentAsString();
    } catch (HttpClientErrorException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getActuatorHealth() {
    try {
      MvcResult mockRes = mockMvc.perform(get("/actuator/health")).andReturn();
      checkResponseCode(mockRes.getResponse());
      return mockRes.getResponse().getContentAsString();
    } catch (HttpClientErrorException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getActuatorPrometheus() {
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

  private String getActuatorMetrics() {
    try {
      MvcResult mockRes = mockMvc.perform(get("/actuator/metrics")).andReturn();
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
      MvcResult mockRes = mockMvc.perform(get("/actuator/metrics/test.application")).andReturn();
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
