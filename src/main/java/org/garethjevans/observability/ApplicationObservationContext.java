package org.garethjevans.observability;

import io.micrometer.observation.Observation;

public class ApplicationObservationContext extends Observation.Context {

    private String lowKeyOne;
    private String lowKeyTwo;
    private String lowKeyThree;
    private String highKeyOne;
    private String highKeyTwo;

    public String getLowKeyOne() {
        return lowKeyOne;
    }

    public void setLowKeyOne(String lowKeyOne) {
        this.lowKeyOne = lowKeyOne;
    }

    public String getLowKeyTwo() {
        return lowKeyTwo;
    }

    public void setLowKeyTwo(String lowKeyTwo) {
        this.lowKeyTwo = lowKeyTwo;
    }

    public String getLowKeyThree() {
        return lowKeyThree;
    }

    public void setLowKeyThree(String lowKeyThree) {
        this.lowKeyThree = lowKeyThree;
    }

    public String getHighKeyOne() {
        return highKeyOne;
    }

    public void setHighKeyOne(String highKeyOne) {
        this.highKeyOne = highKeyOne;
    }

    public String getHighKeyTwo() {
        return highKeyTwo;
    }

    public void setHighKeyTwo(String highKeyTwo) {
        this.highKeyTwo = highKeyTwo;
    }
}
