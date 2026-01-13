package org.garethjevans.observability;

public class TagFilter {

  private String metricName;
  private String tagName;
  private int maxValues = -1;

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public int getMaxValues() {
    return maxValues;
  }

  public void setMaxValues(int maxValues) {
    this.maxValues = maxValues;
  }
}
