package com.sethlee0111.reminiscence.activities;

//@TODO put loading screen while searching for photos
//@TODO put no photos screen when there is no match

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.sethlee0111.reminiscence.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

public class PhotoSlideActivity extends AppCompatActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static int numPages = 0;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private ContextLocationListener locationListener;
    private SimpleTimeZone simpleTimeZone;
    private ContextEntity currentContext;
    private List<ContextEntity> contextList;
    private String path;
    private ImageView imageView;
    private android.os.Handler customHandler;
    /**
     * photos that have close context
     */
    private ArrayList<ContextEntity> contextEntities;
    private ContextDatabaseController controller;
    //    private ContextAsyncTask contextAsyncTask;
    private FindNeighbors findNeighbors;
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();

    /**
     * ArrayList of neighboring devices
     */
    private ArrayList<String> devices = new ArrayList<>();

    /**
     * icons that indicate context relevance
     */
    private ImageView locView;
    private ImageView timeView;
    private ImageView neiView;
    private ImageView wedView;
    private ImageView locTog;
    private ImageView timeTog;
    private ImageView neiTog;
    private ImageView wedTog;

    private static ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_slide);

        locationListener = ContextLocation.getListener(getApplicationContext());
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        simpleTimeZone = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File galleryFolder = new File(storageDirectory, getResources().getString(R.string.app_name));
        path = galleryFolder.getPath();

        /*
         * Initialize icons
         */
        locView = findViewById(R.id.locView);
        timeView = findViewById(R.id.timeView);
        neiView = findViewById(R.id.neiView);
        wedView = findViewById(R.id.wedView);
        locTog = findViewById(R.id.loc_toggle);
        timeTog = findViewById(R.id.time_toggle);
        neiTog = findViewById(R.id.friend_toggle);
        wedTog = findViewById(R.id.weather_toggle);

        locView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(locView);
            }
        });
        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(timeView);
            }
        });
        neiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(neiView);
            }
        });
        wedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(wedView);
            }
        });

        /*
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
        searchMatchingContexts();
        Log.d("CurrentContext", "size : " + contextEntities.size());
        // Instantiate a ViewPager and a PagerAdapter.
        initiateViewPager();
    }

    public void searchMatchingContexts() {
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
        numPages = contextEntities.size();
        // if there are no matching photos
//        if(numPages == 0) {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_photos);
//            File file = new File(Environment.getExternalStorageDirectory(), "no_photos.jpg");
//            try{
//                FileOutputStream outputStream = new FileOutputStream(file);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                outputStream.flush();
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            contextEntities.add(new ContextEntity(file.getAbsolutePath(), new Location("dummy"), new GregorianCalendar()));
//            numPages = 1;
//        }
    }

    public void initiateViewPager() {
        mPager = findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(2);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                triggerIcons(contextEntities.get(i));
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void toggle(View view) {
        if(view == locView) {
            if(ContextAttributes.LOCATION)
                locTog.setImageResource(R.color.disabled);
            else
                locTog.setImageResource(R.color.enabled);
            ContextAttributes.LOCATION = !ContextAttributes.LOCATION;
        }
        else if(view == timeView) {
            if(ContextAttributes.TIME)
                timeTog.setImageResource(R.color.disabled);
            else
                timeTog.setImageResource(R.color.enabled);
            ContextAttributes.TIME = !ContextAttributes.TIME;
        }
        else if(view == neiView) {
            if(ContextAttributes.NEIGHBORS)
                neiTog.setImageResource(R.color.disabled);
            else
                neiTog.setImageResource(R.color.enabled);
            ContextAttributes.NEIGHBORS = !ContextAttributes.NEIGHBORS;
        }
        else if(view == wedView) {
            if(ContextAttributes.WEATHER)
                wedTog.setImageResource(R.color.disabled);
            else
                wedTog.setImageResource(R.color.enabled);
            ContextAttributes.WEATHER = !ContextAttributes.WEATHER;
        }
        searchMatchingContexts();
        initiateViewPager();
    }

    public void triggerIcons(ContextEntity contextEntity) {
        locView.setImageResource(R.drawable.location_grey);
        timeView.setImageResource(R.drawable.time_grey);
        neiView.setImageResource(R.drawable.friend_grey);
        wedView.setImageResource(R.drawable.weather_grey);

        if(currentContext.isSameLocation(contextEntity))
            locView.setImageResource(R.drawable.location_black);
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
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter
     */
    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                ContextEntity contextEntity = contextEntities.get(position);
                return ScreenSlidePageFragment.newInstance(path + "/" + contextEntity.getFilename());
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return ScreenSlidePageFragment.newInstance(null);
            }
        }

        @Override
        public int getCount() {
            return numPages;
        }
    }

}
