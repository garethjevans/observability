package org.garethjevans.observability;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum ApplicationObservationDocumentation implements ObservationDocumentation {

  APPLICATION {
    @Override
    public Class<? extends ObservationConvention<? extends Observation.Context>>
        getDefaultConvention() {
      return ApplicationObservationConvention.class;
    }

    @Override
    public KeyName[] getLowCardinalityKeyNames() {
      return LowCardinalityKeyNames.values();
    }

    @Override
    public KeyName[] getHighCardinalityKeyNames() {
      return HighCardinalityKeyNames.values();
    }
  };

  public enum LowCardinalityKeyNames implements KeyName {
    ONE {
      @Override
      public String asString() {
        return "low.key.one";
      }
    },
    TWO {
      @Override
      public String asString() {
        return "low.key.two";
      }
    },
    THREE {
      @Override
      public String asString() {
        return "low.key.three";
      }
    },
  }

  public enum HighCardinalityKeyNames implements KeyName {
    ONE {
      @Override
      public String asString() {
        return "high.key.one";
      }
    },
    TWO {
      @Override
      public String asString() {
        return "high.key.two";
      }
    },
  }
}
