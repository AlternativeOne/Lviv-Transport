package com.lexoff.lvivtransport.info;

import java.util.ArrayList;

public class RoutingRoutesInfo extends Info {

    private ArrayList<StaticRouteInfo> staticRoute1, staticRoute2;
    private ArrayList<DynamicRouteInfo> dynamicRoute1, dynamicRoute2;

    public RoutingRoutesInfo(){
        staticRoute1=new ArrayList<>();
        staticRoute2=new ArrayList<>();
        dynamicRoute1=new ArrayList<>();
        dynamicRoute2=new ArrayList<>();
    }

    public void addStaticRoute1(StaticRouteInfo info){
        staticRoute1.add(info);
    }

    public ArrayList<StaticRouteInfo> getStaticRoute1() {
        return staticRoute1;
    }

    public void addStaticRoute2(StaticRouteInfo info){
        staticRoute2.add(info);
    }

    public ArrayList<StaticRouteInfo> getStaticRoute2() {
        return staticRoute2;
    }

    public void addDynamicRoute1(DynamicRouteInfo info){
        dynamicRoute1.add(info);
    }

    public ArrayList<DynamicRouteInfo> getDynamicRoute1() {
        return dynamicRoute1;
    }

    public void addDynamicRoute2(DynamicRouteInfo info){
        dynamicRoute2.add(info);
    }

    public ArrayList<DynamicRouteInfo> getDynamicRoute2() {
        return dynamicRoute2;
    }

}
