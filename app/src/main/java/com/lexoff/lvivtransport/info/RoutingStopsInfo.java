package com.lexoff.lvivtransport.info;

public class RoutingStopsInfo extends Info {

    private Stop startStop, endStop;

    public RoutingStopsInfo(){

    }

    public Stop getStartStop() {
        return startStop;
    }

    public void setStartStop(Stop startStop) {
        this.startStop = startStop;
    }

    public Stop getEndStop() {
        return endStop;
    }

    public void setEndStop(Stop endStop) {
        this.endStop = endStop;
    }
}
