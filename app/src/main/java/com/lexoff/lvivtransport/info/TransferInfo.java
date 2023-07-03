package com.lexoff.lvivtransport.info;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class TransferInfo extends Info {
    private String id;
    private String color;
    private String routeShortName;
    private TransportTypeEnum vehicleType;
    private int direction;

    private List<GeoPoint> shapes;

    public TransferInfo(){
        shapes=new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public TransportTypeEnum getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(TransportTypeEnum vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public List<GeoPoint> getShapes() {
        return shapes;
    }

    public void setShapes(List<GeoPoint> shapes) {
        this.shapes = shapes;
    }
}
