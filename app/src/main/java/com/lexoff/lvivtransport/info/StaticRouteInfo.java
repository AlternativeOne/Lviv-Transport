package com.lexoff.lvivtransport.info;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class StaticRouteInfo extends Info {
    private String id;
    private String forwardColor;
    private String backwardColor;
    private TransportTypeEnum type;
    private String routeShortName;
    private String routeLongName;

    private List<Stop> forwardStops;
    private List<Stop> backwardStops;

    private List<GeoPoint> forwardShapes;
    private List<GeoPoint> backwardShapes;

    public StaticRouteInfo(){
        forwardStops=new ArrayList<>();
        backwardStops=new ArrayList<>();

        forwardShapes=new ArrayList<>();
        backwardShapes=new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getForwardColor() {
        if (type.equals(TransportTypeEnum.BUS)){
            return Colors.BUS_FORWARD_COLOR;
        } else if (type.equals(TransportTypeEnum.TRAM)){
            return Colors.TRAM_FORWARD_COLOR;
        } else if (type.equals(TransportTypeEnum.TROL)){
            return Colors.TROL_FORWARD_COLOR;
        }

        return forwardColor;
    }

    public String getBackwardColor() {
        if (type.equals(TransportTypeEnum.BUS)){
            return Colors.BUS_BACKWARD_COLOR;
        } else if (type.equals(TransportTypeEnum.TRAM)){
            return Colors.TRAM_BACKWARD_COLOR;
        } else if (type.equals(TransportTypeEnum.TROL)){
            return Colors.TROL_BACKWARD_COLOR;
        }

        return backwardColor;
    }

    public void setBackwardColor(String backwardColor) {
        this.backwardColor = backwardColor;
    }

    public TransportTypeEnum getType() {
        return type;
    }

    public void setType(TransportTypeEnum type) {
        this.type = type;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
    }

    public void addForwardStop(Stop stop){
        forwardStops.add(stop);
    }

    public List<Stop> getForwardStops(){
        return forwardStops;
    }

    public void addBackwardStop(Stop stop){
        backwardStops.add(stop);
    }

    public List<Stop> getBackwardStops(){
        return backwardStops;
    }

    public void addForwardShape(GeoPoint point){
        forwardShapes.add(point);
    }

    public List<GeoPoint> getForwardShapes() {
        return forwardShapes;
    }

    public void addBackwardShape(GeoPoint point){
        backwardShapes.add(point);
    }

    public List<GeoPoint> getBackwardShapes() {
        return backwardShapes;
    }

}
