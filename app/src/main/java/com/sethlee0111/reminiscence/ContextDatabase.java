package com.sethlee0111.reminiscence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {ContextEntity.class}, version = 1, exportSchema = false)
@TypeConverters({ContextDatabaseConverter.class})
public abstract class ContextDatabase extends RoomDatabase {

    public abstract ContextEntityDao contextEntityDao();

    private static volatile ContextDatabase INSTANCE;

    public static ContextDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ContextDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ContextDatabase.class, "context_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}