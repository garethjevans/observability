package org.garethjevans.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.observation.MeterObservationHandler;
import io.micrometer.observation.Observation;
import java.util.ArrayList;
import java.util.List;

/** A meter observation handler that only includes low cardinality tags. */
public class CustomMeterObservationHandler implements MeterObservationHandler<Observation.Context> {

  private final MeterRegistry meterRegistry;
  private final boolean shouldCreateLongTaskTimer;
  private final String keySuffix;

  public CustomMeterObservationHandler(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    this.shouldCreateLongTaskTimer = true;
    this.keySuffix = "_low_" + System.identityHashCode(meterRegistry);
  }

  @Override
  public void onStart(Observation.Context context) {
    if (shouldCreateLongTaskTimer) {
      LongTaskTimer.Sample longTaskSample =
          LongTaskTimer.builder(context.getName() + ".active")
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
    List<Tag> tags = createTags(context);
    tags.add(Tag.of("error", getErrorValue(context)));

    Timer.Sample sample = context.get(Timer.Sample.class.getName() + keySuffix);
    if (sample != null) {
      sample.stop(Timer.builder(context.getName()).tags(tags).register(this.meterRegistry));
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
    Counter.builder(context.getName() + "." + event.getName())
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
    // Only add low cardinality tags
    for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
      tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
    }
    return tags;
  }

  /**
   * Meter types to ignore.
   *
   * @since 1.13.0
   */
  public enum IgnoredMeters {
    LONG_TASK_TIMER
  }
}
