package org.garethjevans.observability;

/**
 * Describes a filter that can be applied to metrics based on a tag.
 *
 * <p>Each instance identifies a metric by name, the tag to filter on, and an optional upper bound
 * on the number of distinct values allowed for that tag.
 */
public class TagFilter {

  /** Name of the metric to which this filter applies. */
  private String metricName = "";

  /** Name of the tag whose values are used to filter the metric. */
  private String tagName;

  /**
   * Maximum number of distinct values allowed for {@link #tagName} on the given {@link
   * #metricName}. A value of {@code -1} indicates that no limit is applied and the tag values
   * should not be filtered.
   */
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
