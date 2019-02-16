package com.sethlee0111.reminiscence;

import android.location.Location;

import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

enum State {
    REAL_WORLD, SIMULATED_WORLD
}

public class ContextState {
    private static State state = State.REAL_WORLD;
    private static ContextEntity contextEntity= new ContextEntity("fake_context", new Location("fake_provider")
            , new GregorianCalendar(new SimpleTimeZone(-6 * 60 * 60 * 1000, TimeZone.getAvailableIDs(-6 * 60 * 60 * 1000)[0])));

    public static void setContextEntity(ContextEntity contextEntity) {
        ContextState.contextEntity = contextEntity;
    }

    public static ContextEntity getContextEntity() {
        return contextEntity;
    }

    public static void simulated() {state = State.SIMULATED_WORLD;}
    public static void real() {state = State.REAL_WORLD;}
    public static boolean isSimulated() { return (state == State.SIMULATED_WORLD); }
    public static boolean isReal() { return (state == State.REAL_WORLD); }

    public static State getState() {return state;}
}
