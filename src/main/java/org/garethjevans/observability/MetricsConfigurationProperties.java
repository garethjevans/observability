package org.garethjevans.observability;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "metrics")
public class MetricsConfigurationProperties {

  private List<TagFilter> highCardinalityTagFilters = new ArrayList<>();

  private boolean allowHighCardinality = false;

  public List<TagFilter> getHighCardinalityTagFilters() {
    return highCardinalityTagFilters;
  }

  public void setHighCardinalityTagFilters(List<TagFilter> highCardinalityTagFilters) {
    this.highCardinalityTagFilters = highCardinalityTagFilters;
  }

  public boolean isAllowHighCardinality() {
    return allowHighCardinality;
  }

  public void setAllowHighCardinality(boolean allowHighCardinality) {
    this.allowHighCardinality = allowHighCardinality;
  }
}
