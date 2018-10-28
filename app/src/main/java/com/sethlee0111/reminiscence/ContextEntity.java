package com.sethlee0111.reminiscence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import java.util.Calendar;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;


@Entity(tableName = "context_table")
public class ContextEntity {

    @PrimaryKey
    @ColumnInfo
    @NonNull private final String filename;
    @ColumnInfo
    private final Location location;
    @ColumnInfo
    private final Calendar calendar;

    public String getFilename() {
        return filename;
    }

    public Location getLocation() {
        return location;
    }

    public Calendar getCalendar() {
        return calendar;
    }
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
    }

    public double locDistance(ContextEntity ce) {
        double dist = 0;
        try {
            double latDistSqr = Math.pow(location.getLatitude() - ce.getLocation().getLatitude(), 2);
            double longDistSqr = Math.pow(location.getLongitude() - ce.getLocation().getLongitude(), 2);

            dist = Math.sqrt(latDistSqr + longDistSqr);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("ContextEntity.java", "No location");
            return 0;
        }
        return dist;
    }

    @Override
    public String toString() {
        return "Location: Lat:" + location.getLatitude() + " Lang:" + location.getLongitude() + "\nTime: " + calendar.getTime().toString();
    }
}
