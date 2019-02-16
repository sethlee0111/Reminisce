package com.sethlee0111.reminiscence;

import com.sethlee0111.contextdatabase.*;
import android.arch.persistence.room.TypeConverter;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class ContextDatabaseConverter {
    @TypeConverter
    public static Calendar fromStringToCal(String value) {
        Type calType = new TypeToken<Calendar>() {}.getType();
        return new Gson().fromJson(value, calType);
    }

    @TypeConverter
    public static Location fromStringToLoc(String value) {
        Type locType = new TypeToken<Location>() {}.getType();
        return new Gson().fromJson(value, locType);
    }

    @TypeConverter
    public static ArrayList<String> fromStringToNeighbor(String value) {
        Type arrType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, arrType);
    }

    @TypeConverter
    public static String fromLocation(Location location) {
        Gson gson = new Gson();
        String json = gson.toJson(location);
        return json;
    }
    @TypeConverter
    public static String fromCalendar(Calendar calendar) {
        Gson gson = new Gson();
        String json = gson.toJson(calendar);
        return json;
    }
    @TypeConverter
    public static String fromNeighbor(ArrayList<String> neighbor) {
        Gson gson = new Gson();
        String json = gson.toJson(neighbor);
        return json;
    }
}
