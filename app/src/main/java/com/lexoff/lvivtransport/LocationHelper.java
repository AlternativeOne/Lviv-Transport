package com.lexoff.lvivtransport;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

public class LocationHelper {

    @SuppressLint("MissingPermission")
    public static void getLocation(OnLocationFound onSuccess) {
        Context context = App.getApp();

        /*if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)==CommonStatusCodes.SUCCESS) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //TODO: LOW_POWER?
            mLocationRequest.setInterval(5);
            mLocationRequest.setFastestInterval(0);
            mLocationRequest.setNumUpdates(1);

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location mLastLocation = locationResult.getLastLocation();
                    onSuccess.onSuccess(mLastLocation);
                }
            }, Looper.myLooper());
        } else {*/
            LocationManager locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            String providerName;

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                providerName=LocationManager.NETWORK_PROVIDER;
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                providerName=LocationManager.GPS_PROVIDER;
            } else {
                onSuccess.onSuccess(null);

                return;
            }

            locationManager.requestSingleUpdate(providerName, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    onSuccess.onSuccess(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            }, Looper.getMainLooper());
        /*}*/
    }

    public static boolean isLocationEnabled() {
        Context context=App.getApp();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager==null) {
            return false;
        }

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean checkPermissions() {
        Context context=App.getApp();

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 45);
    }

}
