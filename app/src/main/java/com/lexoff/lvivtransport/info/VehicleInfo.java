package com.lexoff.lvivtransport.info;

public class VehicleInfo extends Info {
    private StaticRouteInfo staticRouteInfo;
    private TransportInfo transportInfo;

    public VehicleInfo(){

    }

    public StaticRouteInfo getStaticRouteInfo() {
        return staticRouteInfo;
    }

    public void setStaticRouteInfo(StaticRouteInfo staticRouteInfo) {
        this.staticRouteInfo = staticRouteInfo;
    }

    public TransportInfo getTransportInfo() {
        return transportInfo;
    }

    public void setTransportInfo(TransportInfo transportInfo) {
        this.transportInfo = transportInfo;
    }
}
