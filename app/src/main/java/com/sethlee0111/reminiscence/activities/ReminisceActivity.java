package com.sethlee0111.reminiscence.activities;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sethlee0111.reminiscence.ContextControllerCallback;
import com.sethlee0111.reminiscence.ContextDatabaseController;
import com.sethlee0111.reminiscence.ContextEntity;
import com.sethlee0111.reminiscence.ContextLocation;
import com.sethlee0111.reminiscence.ContextLocationListener;
import com.sethlee0111.reminiscence.ContextState;
import com.sethlee0111.reminiscence.FindNeighborListener;
import com.sethlee0111.reminiscence.FindNeighbors;
import com.sethlee0111.reminiscence.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class ReminisceActivity extends AppCompatActivity {
    private ContextLocationListener locationListener;
    private SimpleTimeZone simpleTimeZone;
    private ContextEntity currentContext;
    private List<ContextEntity> contextList;
    private String path;
    private ImageView imageView;
    private android.os.Handler customHandler;
    private ArrayList<ContextEntity> contextEntities;
    private ContextDatabaseController controller;
//    private ContextAsyncTask contextAsyncTask;
    private FindNeighbors findNeighbors;
    private ArrayList<String> devices = new ArrayList<>();
    private LinearLayout layout;

    private ImageView locView;
    private ImageView timeView;
    private ImageView neiView;
    private ImageView wedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminisce);
        // set progress bar while loading
        layout = findViewById(R.id.ReminiProgress);
        layout.setVisibility(View.VISIBLE);

        locView = findViewById(R.id.locView);
        timeView = findViewById(R.id.timeView);
        neiView = findViewById(R.id.neiView);
        wedView = findViewById(R.id.wedView);

        imageView = findViewById(R.id.remini_view);
        locationListener = ContextLocation.getListener(getApplicationContext());
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        simpleTimeZone = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File galleryFolder = new File(storageDirectory, getResources().getString(R.string.app_name));
        path = galleryFolder.getPath();

        /**
         * Find Neighbors
         */
        findNeighbors = new FindNeighbors(this, new FindNeighborListener() {
            @Override
            public void onFindNeighbor(String neighbor) {
                devices.add(neighbor);
                currentContext.setNeighbors(devices);
            }
        });

        /**
         * Decide which context to use - fake or real?
         */
        contextEntities = new ArrayList<>();
        controller = ContextDatabaseController.getContextDatabaseController(this, getApplicationContext());
        controller.setCallback(new ContextControllerCallback() {
            @Override
            public void onPostQuery() {
                layout.setVisibility(View.GONE);
            }
        });
        if(ContextState.isSimulated()) {
            currentContext = ContextState.getContextEntity();
            Log.d("Context", "Fake Context Mode");
            ArrayList<ContextEntity> allDataList = controller.listAllData(); // Fetch all data from the database
            for (ContextEntity contextEntity : allDataList) {
                Log.d("Context", "Inspect matching file for fake context" + contextEntity.getFilename());
                if (contextEntity.isSameContext(currentContext)) {
                    contextEntities.add(contextEntity);
                }
            }

        } else {
            Log.d("Context", "Real Context Mode");
            contextEntities = controller.listData();
            currentContext = controller.getCurrentContext();
            Log.e("Neighbors", currentContext.getNeighbors().toString());
        }
        Log.d("CurrentContext", "size : " + contextEntities.size());

        /**
         * Start SlidesShow
         */
        customHandler = new Handler();
        updateTimerThread.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CurrentContext", "onResume");
        findNeighbors.onResume();
    }

    private Runnable updateTimerThread = new Runnable()
    {
        int slideIndex = 0;
        public void run()
        {
            locView.setImageResource(R.drawable.location_grey);
            timeView.setImageResource(R.drawable.time_grey);
            neiView.setImageResource(R.drawable.friend_grey);
            wedView.setImageResource(R.drawable.weather_grey);
            try {
                Log.d("SlidesShow", "Photo " + (slideIndex+1) );
                if (slideIndex >= contextEntities.size())
                    slideIndex = 0;
                ContextEntity contextEntity = contextEntities.get(slideIndex++);
                Log.d("SlidesShow", "" + contextEntity.getFilename());
                imageView.setImageBitmap(BitmapFactory.decodeFile(path + "/" + contextEntity.getFilename()));
//                Log.d("SlidesShow" ,"current n: " + currentContext.getNeighbors());
                if(currentContext.isSameLocation(contextEntity))
                    locView.setImageResource(R.drawable.location_grey);
                if(currentContext.isSameTime(contextEntity))
                    timeView.setImageResource(R.drawable.time_black);
                if(currentContext.isSameNeighbors(contextEntity))
                    neiView.setImageResource(R.drawable.friend_black);
                if(currentContext.isSameWeather(contextEntity)) {
                    if(contextEntity.getWeather().equals("Clear"))
                        wedView.setImageResource(R.drawable.sun_black);
                    else if(contextEntity.getWeather().equals("Clouds"))
                        wedView.setImageResource(R.drawable.cloud_black);
                    else if(contextEntity.getWeather().equals("Rain"))
                        wedView.setImageResource(R.drawable.raining_black);
                    else if(contextEntity.getWeather().equals("Snow"))
                        wedView.setImageResource(R.drawable.snow_black);
                }

            } catch (IndexOutOfBoundsException e) {
                //e.printStackTrace();
                Log.e("SlidesShow", "No Photos");
            } finally {
                customHandler.postDelayed(this, 3000);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        contextEntities.clear();
        try {
            customHandler.removeCallbacks(updateTimerThread);
            Log.d("onPause", "customHandler callbacks removed");

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("onPause", "customHandler Null");
        }
        findNeighbors.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        findNeighbors.onDestroy();
    }

    /**
     * Fatal signal 11 (SIGSEGV), code 1, fault addr 0x4000006d5 in tid 17543 (FinalizerDaemon)
     * @TODO Fix it...
     * I get this when I open this activity fast enough
     */
}
