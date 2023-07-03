package com.lexoff.lvivtransport;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class StopOverlayItem extends OverlayItem {

    private int stopId;

    public StopOverlayItem(String aTitle, String aSnippet, IGeoPoint aGeoPoint) {
        super(aTitle, aSnippet, aGeoPoint);
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

}
