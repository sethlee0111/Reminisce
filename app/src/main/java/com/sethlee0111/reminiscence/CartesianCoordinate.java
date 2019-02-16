package com.sethlee0111.reminiscence;

import android.location.Location;

public class CartesianCoordinate {
    private double x;
    private double y;
    private double z;

    public CartesianCoordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * constructor of CartesianCoordinate from location
     * @param location
     */
    public CartesianCoordinate(Location location) {
        double pi = rad(location.getLongitude());
        double theta = rad(location.getLatitude());

        x = Math.sin(theta) * Math.cos(pi);
        y = Math.sin(theta) * Math.sin(pi);
        z = Math.cos(theta);
    }


    private double rad(double degree) {
        return degree / 180 * Math.PI;
    }

    public double dot(CartesianCoordinate cc) {
        return x * cc.x + y * cc.y + z * cc.z;
    }
}
