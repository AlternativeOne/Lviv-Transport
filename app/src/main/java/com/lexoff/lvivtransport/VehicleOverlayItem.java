package com.lexoff.lvivtransport;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class VehicleOverlayItem extends OverlayItem {

    private String code;

    //private int bearing;

    public VehicleOverlayItem(String aTitle, String aSnippet, IGeoPoint aGeoPoint) {
        super(aTitle, aSnippet, aGeoPoint);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /*public int getBearing() {
        return bearing;
    }

    public void setBearing(int bearing) {
        this.bearing = bearing;
    }*/
}
