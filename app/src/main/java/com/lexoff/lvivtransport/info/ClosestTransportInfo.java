package com.lexoff.lvivtransport.info;

import org.osmdroid.util.GeoPoint;

public class ClosestTransportInfo extends Info {
    private String id;
    private String color;
    private int direction;
    private String shortRouteName;
    private TransportTypeEnum type;
    private GeoPoint location;

    private int bearing;

    public ClosestTransportInfo(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public TransportTypeEnum getType() {
        return type;
    }

    public void setType(TransportTypeEnum type) {
        this.type = type;
    }

    public String getShortRouteName() {
        return shortRouteName;
    }

    public void setShortRouteName(String shortRouteName) {
        this.shortRouteName = shortRouteName;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getBearing() {
        return bearing;
    }

    public void setBearing(int bearing) {
        this.bearing = bearing;
    }
}
