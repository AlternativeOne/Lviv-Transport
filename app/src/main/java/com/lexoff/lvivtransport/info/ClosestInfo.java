package com.lexoff.lvivtransport.info;

public class ClosestInfo extends Info {
    private StopsInfo stopsInfo;
    private ClosestTransportsInfo transportsInfo;

    public ClosestInfo(){

    }

    public ClosestTransportsInfo getTransportsInfo() {
        return transportsInfo;
    }

    public void setTransportsInfo(ClosestTransportsInfo transportsInfo) {
        this.transportsInfo = transportsInfo;
    }

    public StopsInfo getStopsInfo() {
        return stopsInfo;
    }

    public void setStopsInfo(StopsInfo stopsInfo) {
        this.stopsInfo = stopsInfo;
    }
}
