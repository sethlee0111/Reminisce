package com.sethlee0111.reminiscence;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class ReminisceActivity extends AppCompatActivity {
    private ContextLocationListener locationListener;
    private SimpleTimeZone simpleTimeZone;
    private ContextEntity currentContext;
    private List<ContextEntity> contextList;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminisce);
        locationListener = ContextLocation.getListener(getApplicationContext());
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        simpleTimeZone = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File galleryFolder = new File(storageDirectory, getResources().getString(R.string.app_name));
        path = galleryFolder.getPath();
        getCurrentContext();
        try {
            contextList = new ContextAsyncTask().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.e("CurrentContext", "No list!");
        }
        Log.d("CurrentContext", "size : " + contextList.size());
        ImageView imageView = findViewById(R.id.Remini1);
        for (ContextEntity contextEntity : contextList) {
            Log.d("CurrentContext", "Looking for File " + path + "/" + contextEntity.getFilename());
            double dist = contextEntity.locDistance(currentContext);
            if (dist < 0.0005) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(path + "/" + contextEntity.getFilename()));
                Log.d("CurrentContext", "Photo Found! Dist : " + dist);
            } else {
                Log.d("CurrrentContext" ,"Distance too far : " + dist);
            }
        }
    }

    private static class ContextAsyncTask extends AsyncTask<Void, Void, List<ContextEntity>> {
        @Override
        protected List<ContextEntity> doInBackground(Void... params) {
            ContextEntityDao contextEntityDao = ContextDatabase.getDatabase(MyApplication.getAppContext()).contextEntityDao();
            return contextEntityDao.getAllContext();
        }
    }

    private void getCurrentContext() {
        currentContext = new ContextEntity("Current_Context", locationListener.getLocation(), new GregorianCalendar(simpleTimeZone));
        Log.d("CurrentContext" , "got context");
    }
}
