package com.sethlee0111.reminiscence;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class ContextLocationListener implements LocationListener {
    private Location location;

    @Override
    public void onLocationChanged(final Location location) {
        this.location = location;
        Log.d("LocationListener", "Location has changed to :" + location.toString());
    }
    @Override
    public void onProviderEnabled(String provider) {
        Log.d("LocationListener", "Provider Enabled");
    }
    @Override
    public void onProviderDisabled(String provider) {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){

    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {this.location = location; }
}
