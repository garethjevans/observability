package org.garethjevans.observability;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public abstract class AbstractCardinalityTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(HighCardinalityTest.class);

  @Autowired protected MockMvc mockMvc;

  protected String getLowScrapeEndpoint() {
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

  protected String getHighScrapeEndpoint() {
    try {
      MvcResult mockRes =
          mockMvc.perform(get("/actuator/prometheus-all").accept(MediaType.TEXT_PLAIN)).andReturn();
      checkResponseCode(mockRes.getResponse());
      return mockRes.getResponse().getContentAsString();
    } catch (HttpClientErrorException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String getActuatorMetricsForTestApplication() {
    try {
      MvcResult mockRes =
          mockMvc
              .perform(get("/actuator/metrics/test.application").accept(MediaType.APPLICATION_JSON))
              .andReturn();
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
