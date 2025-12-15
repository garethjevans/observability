package org.garethjevans.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.observation.MeterObservationHandler;
import io.micrometer.observation.Observation;

import java.util.ArrayList;
import java.util.List;

public class CustomMeterObservationHandler implements MeterObservationHandler<Observation.Context> {

    private final MeterRegistry meterRegistry;

    private final boolean shouldCreateLongTaskTimer;

    public CustomMeterObservationHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.shouldCreateLongTaskTimer = true;
    }

    @Override
    public void onStart(Observation.Context context) {
        if (shouldCreateLongTaskTimer) {
            LongTaskTimer.Sample longTaskSample = LongTaskTimer.builder(context.getName() + ".active")
                    .tags(createTags(context))
                    .register(meterRegistry)
                    .start();
            context.put(LongTaskTimer.Sample.class, longTaskSample);
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        context.put(Timer.Sample.class, sample);
    }

    @Override
    public void onStop(Observation.Context context) {
        List<Tag> tags = createTags(context);
        tags.add(Tag.of("error", getErrorValue(context)));
        Timer.Sample sample = context.getRequired(Timer.Sample.class);
        sample.stop(Timer.builder(context.getName()).tags(tags).register(this.meterRegistry));

        if (shouldCreateLongTaskTimer) {
            LongTaskTimer.Sample longTaskSample = context.getRequired(LongTaskTimer.Sample.class);
            longTaskSample.stop();
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
        for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
            tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
        }
        for (KeyValue keyValue : context.getHighCardinalityKeyValues()) {
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
