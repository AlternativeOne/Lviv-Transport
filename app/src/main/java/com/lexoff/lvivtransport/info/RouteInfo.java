package com.lexoff.lvivtransport.info;

public class RouteInfo extends Info {
    private StaticRouteInfo staticRouteInfo;
    private DynamicRouteInfo dynamicRouteInfo;

    public RouteInfo(){

    }

    public StaticRouteInfo getStaticRouteInfo() {
        return staticRouteInfo;
    }

    public void setStaticRouteInfo(StaticRouteInfo staticRouteInfo) {
        this.staticRouteInfo = staticRouteInfo;
    }

    public DynamicRouteInfo getDynamicRouteInfo() {
        return dynamicRouteInfo;
    }

    public void setDynamicRouteInfo(DynamicRouteInfo dynamicRouteInfo) {
        this.dynamicRouteInfo = dynamicRouteInfo;
    }
}
