package com.lexoff.lvivtransport.info;

public class RailroadStationInfo extends Info {

    private double latitutde;
    private double longitude;
    private String name;

    public RailroadStationInfo(){}

    public double getLatitutde() {
        return latitutde;
    }

    public void setLatitutde(double latitutde) {
        this.latitutde = latitutde;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
