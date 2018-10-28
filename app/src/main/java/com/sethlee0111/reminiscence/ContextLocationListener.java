package com.sethlee0111.reminiscence;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

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
