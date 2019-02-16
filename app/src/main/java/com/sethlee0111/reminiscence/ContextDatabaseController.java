package com.sethlee0111.reminiscence;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

//@TODO change to normal threads. UI doesn't have to stop, for example after taking a photo

/**
 * Middleware that controls context-aware database
 * It includes current context
 * It lets users to store data and query data from ContextDatabase
 */
public class ContextDatabaseController {
    private static ContextDatabaseController contextDatabaseController = null; // Singleton instance
    private ContextLocationListener locationListener = null;
    private ContextControllerCallback controllerCallback;
    private boolean isCallbackEnabled = false;
    private Context context;
    private ContextEntity currentContext;
    private FindNeighbors findNeighbors;
    private ArrayList<String> nList = new ArrayList<>();
    private final ArrayList<String> wedList = new ArrayList<String>(){{
        add("Clear");
        add("Clouds");
        add("Rain");
        add("Snow");
    }};

    private ContextDatabaseController(Activity activity, Context context) {
        this.context = context;
        locationListener = ContextLocation.getListener(context);
//        findNeighbors = new FindNeighbors(activity, new FindNeighborListener() {
//            @Override
//            public void onFindNeighbor(String neighbor) {
//                if(!nList.contains(neighbor)) {
//                    nList.add(neighbor);
//                    updateCurrentContext();
//                    Log.e("found", nList.toString());
//                }
//            }
//        });
//        findNeighbors.onResume();
//        nList.addAll(findNeighbors.getDevices());
        updateCurrentContext();

    }

    public boolean isCallbackEnabled() {
        return isCallbackEnabled;
    }

    public void setCallback(ContextControllerCallback controllerCallback) {
        this.controllerCallback = controllerCallback;
        isCallbackEnabled = true;
    }

    /**
     * Get an instance of ContextDatabaseController
     * @param context pass argument with getApplicationContext()
     */
    public static ContextDatabaseController getContextDatabaseController(Activity activity, Context context) {
        if(contextDatabaseController == null)
            contextDatabaseController = new ContextDatabaseController(activity, context);
        return contextDatabaseController;
    }
    public ArrayList<String> getCurrentNeighbors() {
        return nList;
    }

    public void addData(final ContextEntity contextEntity) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    ContextEntityDao contextEntityDao = ContextDatabase.getDatabase(MyApplication.getAppContext()).contextEntityDao();
                    contextEntityDao.addContext(contextEntity);
                    Log.d("Thread", "added data");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
    public ContextEntity queryData(String fileName) {
        ContextEntity contextEntity = null;
        try {
            contextEntity = new ContextQueryTask(fileName).execute().get();
            Log.d("Context DB", "Successfully queried");
        } catch (Exception e) {
            Log.e("Context DB", "Query failed");
            e.printStackTrace();
        }
        return contextEntity;
    }
    public ArrayList<ContextEntity> listData() {
        List<ContextEntity> list = null;
        try {
            list = new ContextListTask().execute().get();
            Log.d("Context DB List", "Successfully queried");
        } catch (Exception e) {
            Log.e("Context DB List", "Query failed");
            e.printStackTrace();
        }
        ArrayList<ContextEntity> contextEntities = new ArrayList<>();

        updateCurrentContext();

        for (ContextEntity contextEntity : list) {
            Log.d("Context DB", "Looking for File " + contextEntity.getFilename());
            if (contextEntity.isSameContext(currentContext)) {
                Log.d("Context Match", "for file " + contextEntity.getFilename());
                contextEntities.add(contextEntity);
            }
        }
        if(isCallbackEnabled && controllerCallback != null) {
            controllerCallback.onPostQuery();
        }
        return contextEntities;
    }
    public ArrayList<ContextEntity> listAllData() {
        ArrayList<ContextEntity> list = new ArrayList<>();
        try {
            list.addAll(new ContextListTask().execute().get());
            Log.d("Context DB List", "Successfully queried");
        } catch (Exception e) {
            Log.e("Context DB List", "Query failed");
            e.printStackTrace();
        }
        if(isCallbackEnabled && controllerCallback != null) {
            controllerCallback.onPostQuery();
        }
        return list;
    }

    public void updateCurrentContext() {
        locationListener = ContextLocation.getListener(context);
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        SimpleTimeZone simpleTimeZone = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
        // update location and time
        currentContext = new ContextEntity("Current_Context", locationListener.getLocation(), new GregorianCalendar(simpleTimeZone));
        // update neighbors
        currentContext.setNeighbors(nList);
        // update weather
        try {
            String wed = new WeatherWatchTask().execute(locationListener.getLocation().getLatitude(), locationListener.getLocation().getLongitude()).get();
            Log.d("Weather", "fetched :" + wed);
            if(!wedList.contains(wed))
                wed = "Clear";
            currentContext.setWeather(wed);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.e("Weather", "Failed to get info");
        }
    }

    public ContextEntity getCurrentContext() {
        return currentContext;
    }

    /**
     * AsyncTask for adding context data
     */
    private static class ContextAddTask extends AsyncTask<Void, Void, Void> {

        private ContextEntity contextEntity;

        public ContextAddTask(ContextEntity contextEntity) {
            this.contextEntity = contextEntity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContextEntityDao contextEntityDao = ContextDatabase.getDatabase(MyApplication.getAppContext()).contextEntityDao();
            contextEntityDao.addContext(contextEntity);
            return null;
        }
    }

    /**
     * Async Task to query context data from the database
     */
    private static class ContextQueryTask extends AsyncTask<Void, Void, ContextEntity> {

        private ContextEntity contextEntity;
        private String fileName;

        public ContextQueryTask(String fileName) {
            this.fileName = fileName;
        }

        @Override
        protected ContextEntity doInBackground(Void... params) {
            ContextEntityDao contextEntityDao = ContextDatabase.getDatabase(MyApplication.getAppContext()).contextEntityDao();
            try {
                contextEntity = contextEntityDao.getContextFromFileName(fileName).get(0); //@TODO Resolve Error
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                Log.e("PhotoContextSpec", "No context found for " + fileName);
                return null;
            }
            return contextEntity;
        }
    }
    private static class ContextListTask extends AsyncTask<Void, Void, List<ContextEntity>> {
        @Override
        protected List<ContextEntity> doInBackground(Void... params) {
            ContextEntityDao contextEntityDao = ContextDatabase.getDatabase(MyApplication.getAppContext()).contextEntityDao();
            Log.d("ContextEntityDao", "Fetch Data Complete");
            return contextEntityDao.getAllContext();
        }
    }
}
