package com.sethlee0111.reminiscence;

public class ContextAttributes {
    public static boolean LOCATION = true;
    public static boolean TIME = true;
    public static boolean NEIGHBORS = true;
    public static boolean WEATHER = true;

    public static double LOCATION_WEIGHT = 1;
    public static double TIME_WEIGHT = 1;
    public static double NEIGHBORS_WEIGHT = 1;
    public static double WEATHER_WEIGHT = 1;

    public static double contextRelevance(double loc_w, double time_w, double nei_w, double wea_w) {
        double res = 0;
        double contextWeight = 0;
        if(LOCATION) {
            res += loc_w * LOCATION_WEIGHT;
            contextWeight += LOCATION_WEIGHT;
        }
        if(TIME) {
            res += time_w * TIME_WEIGHT;
            contextWeight += LOCATION_WEIGHT;
        }
        if(NEIGHBORS) {
            res += nei_w * NEIGHBORS_WEIGHT;
            contextWeight += LOCATION_WEIGHT;
        }
        if(WEATHER) {
            res += wea_w * WEATHER_WEIGHT;
            contextWeight += LOCATION_WEIGHT;
        }
        if(contextWeight == 0)  // no context applied
            return 0;
        return res / contextWeight;
    }
}
