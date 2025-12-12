package org.garethjevans.observability;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class TestApplicationObservationConvention
    implements ObservationConvention<TestApplicationObservationContext> {

  @Override
  public boolean supportsContext(Observation.Context context) {
    return context instanceof TestApplicationObservationContext;
  }

  @Override
  public KeyValues getLowCardinalityKeyValues(TestApplicationObservationContext context) {
    return KeyValues.of(
        TestApplicationObservationDocumentation.LowCardinalityKeyNames.ONE.asString(),
        context.getLowKeyOne(),
        TestApplicationObservationDocumentation.LowCardinalityKeyNames.TWO.asString(),
        context.getLowKeyTwo(),
        TestApplicationObservationDocumentation.LowCardinalityKeyNames.THREE.asString(),
        context.getLowKeyThree());
  }

  @Override
  public KeyValues getHighCardinalityKeyValues(TestApplicationObservationContext context) {
    return KeyValues.of(
        TestApplicationObservationDocumentation.HighCardinalityKeyNames.ONE.asString(),
        context.getHighKeyOne(),
        TestApplicationObservationDocumentation.HighCardinalityKeyNames.TWO.asString(),
        context.getHighKeyTwo());
  }

  @Override
  public String getName() {
    return "test.application";
  }
}
