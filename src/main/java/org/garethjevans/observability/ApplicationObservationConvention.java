package org.garethjevans.observability;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class ApplicationObservationConvention
    implements ObservationConvention<ApplicationObservationContext> {

  @Override
  public boolean supportsContext(Observation.Context context) {
    return context instanceof ApplicationObservationContext;
  }

  @Override
  public KeyValues getLowCardinalityKeyValues(ApplicationObservationContext context) {
    return KeyValues.of(
        ApplicationObservationDocumentation.LowCardinalityKeyNames.ONE.asString(),
        context.getLowKeyOne(),
        ApplicationObservationDocumentation.LowCardinalityKeyNames.TWO.asString(),
        context.getLowKeyTwo(),
        ApplicationObservationDocumentation.LowCardinalityKeyNames.THREE.asString(),
        context.getLowKeyThree());
  }

  @Override
  public KeyValues getHighCardinalityKeyValues(ApplicationObservationContext context) {
    return KeyValues.of(
        ApplicationObservationDocumentation.HighCardinalityKeyNames.ONE.asString(),
        context.getHighKeyOne(),
        ApplicationObservationDocumentation.HighCardinalityKeyNames.TWO.asString(),
        context.getHighKeyTwo());
  }

  @Override
  public String getName() {
    return "test.application";
  }
}
