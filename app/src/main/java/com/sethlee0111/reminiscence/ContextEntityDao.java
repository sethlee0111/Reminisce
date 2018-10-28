package com.sethlee0111.reminiscence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.Update;
import android.location.Location;

import com.google.gson.Gson;

import java.util.List;

@Dao
public interface ContextEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addContext(ContextEntity entity);

    @Query("select * from context_table")
    public List<ContextEntity> getAllContext();

    @Query("select * from context_table where filename = :fileName")
    public List<ContextEntity> getContextFromFileName(String fileName);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateContext(ContextEntity entity);

    @Query("delete from context_table")
    void removeAllContexts();
}

