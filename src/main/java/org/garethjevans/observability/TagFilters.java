package org.garethjevans.observability;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "metrics.high-cardinality-tag-filters")
public class TagFilters {

  private List<TagFilter> filters;

  public List<TagFilter> getFilters() {
    return filters;
  }

  public void setFilters(List<TagFilter> filters) {
    this.filters = filters;
  }
}
