package com.sethlee0111.reminiscence;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class PhotoContextSpecActivity extends AppCompatActivity implements OnMapReadyCallback{
    private MapView mapView;
    private GoogleMap mMap;
    private ContextEntity photoContext;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_context_spec);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("FilePath"); // get AbsoluteFilePath
        Log.d("PhotoContextSpec", "FilePath found: " + filePath);
        ImageView imageView = findViewById(R.id.photo_view);
        imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
        File f = new File(filePath);
        String fileName = f.getName();
        //fileName = fileName.substring(0, fileName.lastIndexOf('.')); // drop .jpg extension
        Log.d("PhotoContextSpec", "FileName found: " + fileName);
        ContextAsyncTask contextAsyncTask = new ContextAsyncTask(fileName);
        try{
            photoContext = contextAsyncTask.execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        TextView textView = findViewById(R.id.photoInfoView);
        Log.d("GotContext", photoContext.toString());

        try { ;
            textView.setText(photoContext.toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
            textView.setText(R.string.no_context_error);
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    /**
     * Async Task to query context data from the database
     */
    private static class ContextAsyncTask extends AsyncTask<Void, Void, ContextEntity> {

        private ContextEntity contextEntity;
        private String fileName;

        public ContextAsyncTask(String fileName) {
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
            }
            return contextEntity;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(12);
        mMap.setIndoorEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        LatLng photoLoc = new LatLng(photoContext.getLocation().getLatitude(), photoContext.getLocation().getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(photoLoc);
        mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(photoLoc));
    }
}
