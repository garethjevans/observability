package org.garethjevans.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.observation.MeterObservationHandler;
import io.micrometer.observation.Observation;
import java.util.ArrayList;
import java.util.List;

/**
 * A meter observation handler that prefixes metric names with "high." to avoid conflicts
 * with low cardinality metrics.
 */
public class HighCardinalityMeterObservationHandler
    implements MeterObservationHandler<Observation.Context> {

  private final MeterRegistry meterRegistry;
  private final boolean shouldCreateLongTaskTimer;
  private final String keySuffix;

  public HighCardinalityMeterObservationHandler(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    this.shouldCreateLongTaskTimer = true;
    this.keySuffix = "_high_" + System.identityHashCode(meterRegistry);
  }

  @Override
  public void onStart(Observation.Context context) {
    String metricName = "high." + context.getName();

    if (shouldCreateLongTaskTimer) {
      LongTaskTimer.Sample longTaskSample =
          LongTaskTimer.builder(metricName + ".active")
              .tags(createTags(context))
              .register(meterRegistry)
              .start();
      context.put(LongTaskTimer.Sample.class.getName() + keySuffix, longTaskSample);
    }

    Timer.Sample sample = Timer.start(meterRegistry);
    context.put(Timer.Sample.class.getName() + keySuffix, sample);
  }

  @Override
  public void onStop(Observation.Context context) {
    String metricName = "high." + context.getName();
    List<Tag> tags = createTags(context);
    tags.add(Tag.of("error", getErrorValue(context)));

    Timer.Sample sample = context.get(Timer.Sample.class.getName() + keySuffix);
    if (sample != null) {
      sample.stop(Timer.builder(metricName).tags(tags).register(this.meterRegistry));
    }

    if (shouldCreateLongTaskTimer) {
      LongTaskTimer.Sample longTaskSample =
          context.get(LongTaskTimer.Sample.class.getName() + keySuffix);
      if (longTaskSample != null) {
        longTaskSample.stop();
      }
    }
  }

  @Override
  public void onEvent(Observation.Event event, Observation.Context context) {
    String metricName = "high." + context.getName();
    Counter.builder(metricName + "." + event.getName())
        .tags(createTags(context))
        .register(meterRegistry)
        .increment();
  }

  private String getErrorValue(Observation.Context context) {
    Throwable error = context.getError();
    return error != null ? error.getClass().getSimpleName() : KeyValue.NONE_VALUE;
  }

  private List<Tag> createTags(Observation.Context context) {
    List<Tag> tags = new ArrayList<>();
    // Add low cardinality tags
    for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
      tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
    }
    // Add high cardinality tags
    for (KeyValue keyValue : context.getHighCardinalityKeyValues()) {
      tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
    }
    return tags;
  }
}
