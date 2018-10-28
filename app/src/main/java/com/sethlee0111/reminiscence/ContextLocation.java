package com.sethlee0111.reminiscence;

import android.content.Context;

public class ContextLocation {

    private static volatile ContextLocationListener INSTANCE;

    static ContextLocationListener getListener(final Context context) {
        if (INSTANCE == null) {
            synchronized (ContextDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ContextLocationListener();
                }
            }
        }
        return INSTANCE;
    }
}