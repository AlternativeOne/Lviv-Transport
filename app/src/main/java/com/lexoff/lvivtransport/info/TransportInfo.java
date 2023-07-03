package com.lexoff.lvivtransport.info;

import org.osmdroid.util.GeoPoint;

public class TransportInfo extends Info {
    private String id;
    private String shortRouteName;
    private int direction;
    private GeoPoint location;

    private int bearing;

    public TransportInfo(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getShortRouteName() {
        return shortRouteName;
    }

    public void setShortRouteName(String shortRouteName) {
        this.shortRouteName = shortRouteName;
    }

    public int getBearing() {
        return bearing;
    }

    public void setBearing(int bearing) {
        this.bearing = bearing;
    }
}
