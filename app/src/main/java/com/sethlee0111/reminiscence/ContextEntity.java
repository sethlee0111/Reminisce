package com.sethlee0111.reminiscence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Calendar;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

// @TODO Enable user feedback through clicking reminisce button -> change weights

@Entity(tableName = "context_table")
public class ContextEntity {

    @PrimaryKey
    @ColumnInfo
    @NonNull private final String filename;
    @ColumnInfo
    private Location location;
    @ColumnInfo
    private Calendar calendar;
    @ColumnInfo
    private ArrayList<String> neighbors = new ArrayList<>();
    @ColumnInfo
    private String weather;

    public String getFilename() {
        return filename;
    }

    public Location getLocation() {
        return location;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public ArrayList<String> getNeighbors() {return neighbors;}

    public String getWeather() {return weather;}
    /**
     * Entity for context data
     * /@param id : the file id
     * @param filename : name of the photo file
     * /@param loc : location data
     * /@param neighbors : neighbor data
     * /@param ble : BLE data
     */
    public ContextEntity(@NonNull String filename, Location location, Calendar calendar) {
        this.filename = filename;
        this.location = location;
        this.calendar = calendar;
        weather = "Clear";
    }
    public ContextEntity(@NonNull String filename, ContextEntity contextEntity) {
        this.filename = filename;
        this.location = contextEntity.getLocation();
        this.calendar = contextEntity.getCalendar();
        this.neighbors = contextEntity.getNeighbors();
        this.weather = contextEntity.getWeather();
    }

    public void setNeighbors(ArrayList<String> neighbors) {
        this.neighbors = neighbors;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public void setWeather(String weather) {this.weather = weather;}

    /*
    Functions that compute relevance of contexts
    (Not Relevant) 0 <= Relevance <= 1 (Most Relevant)
     */

    public double locRelevance(ContextEntity ce) {
        try {
            CartesianCoordinate this_cc = new CartesianCoordinate(location);
            CartesianCoordinate other_cc = new CartesianCoordinate(ce.location);

            return Math.abs(this_cc.dot(other_cc));

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("ContextEntity.java", "No location");
            return 0;
        }
    }

    public double timeRelevance(ContextEntity ce) {
        double this_theta = this.calendar.get(Calendar.HOUR_OF_DAY) / 23.0 / (2.0 * Math.PI);
        double other_theta = ce.calendar.get(Calendar.HOUR_OF_DAY) / 23.0 / (2.0 * Math.PI);

        return Math.abs(Math.cos(this_theta - other_theta));
    }

    public double neighborRelevance(ContextEntity ce) {
        double num = numberOfNeighbors(ce);
        return num / getNeighbors().size();
    }

    public double weatherRelevance(ContextEntity ce) {
        if(weather.equals(ce.getWeather())) {
            return 1;
        }
        else {
            return 0;
        }
    }

    /*
    isSame() functions
     */
    public boolean isSameContext(ContextEntity ce) {
        double rev = ContextAttributes.contextRelevance(locRelevance(ce), timeRelevance(ce), neighborRelevance(ce), weatherRelevance(ce));
        return rev > 0.5;
    }
    public boolean isSameLocation(ContextEntity ce) {
        double rev = locRelevance(ce);

        return (rev > 0.990);
    }
    public boolean isSameTime(ContextEntity ce) {
        double timeRelevance = timeRelevance(ce);
        return timeRelevance > 0.9;
    }
    public boolean isSameNeighbors(ContextEntity ce) {
        return (neighborRelevance(ce) > 0);
        // @TODO more people user knows, less relevance a certain group of neighbors will have. How to solve this?
        }
    public boolean isSameWeather(ContextEntity ce) {
        return (weatherRelevance(ce) >= 1);
    }

    public int numberOfNeighbors(ContextEntity ce) {
        int res = 0;
        ArrayList<String> n2 = ce.getNeighbors();
        for(String n : n2) {
            if(neighbors.contains(n))
                res++;
        }
        return res;
    }

    @Override
    public String toString() {
        return "Lat:" + location.getLatitude() + "\nLang:" + location.getLongitude()
                + "\nTime: " + calendar.getTime().toString() + "\nNeighbors: " + neighbors.size()
                + "\nWeather: " + weather;
    }
}
