package com.lexoff.lvivtransport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

public class ResourcesManager {
    private Context context;

    private Drawable stopMarker, selectedStopMarker, routingSelectedStopMarker, addressMarker, forwardTransportMarker, backwardTransportMarker, railwayStationMarker, locationMarker;

    public ResourcesManager(Context context) {
        this.context = context;
    }

    public void init() {
        Bitmap stopBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.non_selected_stop);
        Bitmap stopScaledBitmap=Bitmap.createScaledBitmap(stopBitmap, (int)(stopBitmap.getWidth()/1.5), (int)(stopBitmap.getHeight()/1.5), true);
        stopMarker = new BitmapDrawable(context.getResources(), stopScaledBitmap);

        Bitmap selectedStopBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.selected_stop);
        Bitmap selectedStopScaledBitmap=Bitmap.createScaledBitmap(selectedStopBitmap, (int)(selectedStopBitmap.getWidth()/1.5), (int)(selectedStopBitmap.getHeight()/1.5), true);
        selectedStopMarker = new BitmapDrawable(context.getResources(), selectedStopScaledBitmap);

        Bitmap routingSelectedStopBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.routing_selected_stop);
        Bitmap routingSelectedStopScaledBitmap=Bitmap.createScaledBitmap(routingSelectedStopBitmap, (int)(routingSelectedStopBitmap.getWidth()/1.5), (int)(routingSelectedStopBitmap.getHeight()/1.5), true);
        routingSelectedStopMarker = new BitmapDrawable(context.getResources(), routingSelectedStopScaledBitmap);

        Bitmap addressMarkerBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.address_marker);
        Bitmap addressMarkerScaledBitmap=Bitmap.createScaledBitmap(addressMarkerBitmap, (int)(addressMarkerBitmap.getWidth()/1.6), (int)(addressMarkerBitmap.getHeight()/1.6), true);
        addressMarker = new BitmapDrawable(context.getResources(), addressMarkerScaledBitmap);

        forwardTransportMarker = AppCompatResources.getDrawable(context, R.drawable.green_transport_icon);
        backwardTransportMarker = AppCompatResources.getDrawable(context, R.drawable.blue_transport_icon);

        Bitmap railwayStationMarkerBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.railway_station);
        Bitmap railwayStationMarkerScaledBitmap=Bitmap.createScaledBitmap(railwayStationMarkerBitmap, (int)(railwayStationMarkerBitmap.getWidth()/2.5), (int)(railwayStationMarkerBitmap.getHeight()/2.5), true);
        railwayStationMarker = new BitmapDrawable(context.getResources(), railwayStationMarkerScaledBitmap);

        Bitmap locationMarkerBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.location_marker);
        Bitmap locationMarkerScaledBitmap=Bitmap.createScaledBitmap(locationMarkerBitmap, (int)(locationMarkerBitmap.getWidth()/3.5), (int)(locationMarkerBitmap.getHeight()/3.5), true);
        locationMarker = new BitmapDrawable(context.getResources(), locationMarkerScaledBitmap);
    }

    public Drawable getStopMarker() {
        return stopMarker;
    }

    public Drawable getSelectedStopMarker(){
        return selectedStopMarker;
    }

    public Drawable getRoutingSelectedStopMarker(){
        return routingSelectedStopMarker;
    }

    public Drawable getAddressMarker(){
        return addressMarker;
    }

    public Drawable getRailwayStationMarker(){
        return railwayStationMarker;
    }

    public Drawable getLocationMarker(){
        return locationMarker;
    }

    public Drawable getTransportMarker(int direction) {
        return (direction == 0) ? forwardTransportMarker : backwardTransportMarker;
    }

    public Drawable getRotatedTransportMarker(int direction, int angle) {
        Bitmap source = BitmapFactory.decodeResource(context.getResources(), direction == 0 ? R.drawable.green_transport_icon_unrotated : R.drawable.blue_transport_icon);

        Matrix matrix = new Matrix();
        matrix.postRotate((float) angle);
        Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return new BitmapDrawable(context.getResources(), rotated);
    }

}
