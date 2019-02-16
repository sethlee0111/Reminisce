package com.sethlee0111.reminiscence.activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sethlee0111.reminiscence.ContextEntity;
import com.sethlee0111.reminiscence.ContextLocation;
import com.sethlee0111.reminiscence.ContextLocationListener;
import com.sethlee0111.reminiscence.ContextState;
import com.sethlee0111.reminiscence.DatePickerFragment;
import com.sethlee0111.reminiscence.OnDataPass;
import com.sethlee0111.reminiscence.R;
import com.sethlee0111.reminiscence.TimePickerFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class ModifyContextActivity extends AppCompatActivity implements OnDataPass, OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap mMap;
    TextView textTime;
    TextView textDate;
    private ContextLocationListener locationListener;
    private Switch contextSwitch;
    private Switch aliceSwitch;
    private Switch bobSwitch;
    private Switch chloeSwitch;

    private ContextEntity contextEntity;
    private ContextEntity fakeContextEntity;
    private SimpleTimeZone simpleTimeZone;
    private ArrayList<String> fakeNeighborList = new ArrayList<>();

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_context);

        contextSwitch = findViewById(R.id.my_context_switch);
        contextSwitch.setChecked(ContextState.isSimulated());
        contextSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    ContextState.simulated();
                } else {
                    ContextState.real();
                }
            }
        });
        fakeContextEntity = ContextState.getContextEntity();
        ArrayList<String> n_list = fakeContextEntity.getNeighbors();
        aliceSwitch = findViewById(R.id.alice);
        bobSwitch = findViewById(R.id.bob);
        chloeSwitch = findViewById(R.id.chloe);
        if(n_list.contains("Alice")) {
            aliceSwitch.setChecked(true);
            fakeNeighborList.add("Alice");
        }
        if(n_list.contains("Bob")) {
            bobSwitch.setChecked(true);
            fakeNeighborList.add("Bob");

        }
        if(n_list.contains("Chloe")) {
            chloeSwitch.setChecked(true);
            fakeNeighborList.add("Chloe");
        }

        String[] ids = TimeZone.getAvailableIDs(-6 * 60 * 60 * 1000);
        simpleTimeZone = new SimpleTimeZone(-6 * 60 * 60 * 1000, ids[0]);
        locationListener = ContextLocation.getListener(getApplicationContext());

        try {
            contextEntity = new ContextEntity("fake_context", locationListener.getLocation(), new GregorianCalendar(simpleTimeZone));
            ContextState.setContextEntity(contextEntity);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("No Loc", "for contextEntity");
        }
        aliceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(!fakeNeighborList.contains("Alice"))
                        fakeNeighborList.add("Alice");
                } else {
                    if(fakeNeighborList.contains("Alice"))
                        fakeNeighborList.remove("Alice");
                }
                contextEntity.setNeighbors(fakeNeighborList);
            }
        });
        bobSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(!fakeNeighborList.contains("Bob"))
                        fakeNeighborList.add("Bob");
                } else {
                    if(fakeNeighborList.contains("Bob"))
                        fakeNeighborList.remove("Bob");
                }
                contextEntity.setNeighbors(fakeNeighborList);
            }
        });
        chloeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(!fakeNeighborList.contains("Chloe"))
                        fakeNeighborList.add("Chloe");
                } else {
                    if(fakeNeighborList.contains("Chloe"))
                        fakeNeighborList.remove("Chloe");
                }
                contextEntity.setNeighbors(fakeNeighborList);
            }
        });

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.mapView2);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        Spinner spinner = (Spinner) findViewById(R.id.weather_spinner);
        spinner.setOnItemSelectedListener(new ItemSelectedListener());
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weather_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private class ItemSelectedListener implements AdapterView.OnItemSelectedListener {
        String weathers[] = {"Clear", "Clouds", "Rain", "Snow"};
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            contextEntity.setWeather(weathers[pos]);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            contextEntity.setWeather(weathers[0]);
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

        LatLng loc = null;
        try {
            fakeContextEntity = ContextState.getContextEntity();
            loc = new LatLng(fakeContextEntity.getLocation().getLatitude(), fakeContextEntity.getLocation().getLongitude());

        } catch (NullPointerException e) {
            e.printStackTrace();
            try {
                if(locationListener.getLocation() == null)
                    finish();
                loc = new LatLng(locationListener.getLocation().getLatitude(), locationListener.getLocation().getLongitude());
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                finish();
            }
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);
        if(loc != null)
            markerOptions.position(loc);
        else
            finish();      // @TODO properly handle null location
        mMap.addMarker(markerOptions);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // Nothing to do
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Nothing to do
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng curLoc = marker.getPosition();
                Location location = contextEntity.getLocation();
                location.setLatitude(curLoc.latitude);
                location.setLongitude(curLoc.longitude);
                contextEntity.setLocation(location);
            }
        });
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
    public void confirmContext(View view) {
        try {
            ContextState.setContextEntity(contextEntity);
        } catch (NullPointerException e) {
            e.printStackTrace();
            //@TODO no context
        }
    }

    @Override
    public void onDataPass(String... data) { /* Do nothing */ }

    @Override
    public void onTimePass(int hourOfDay, int minute) {
        // change textView
        TextView timeView = findViewById(R.id.textTime);
        timeView.setText(getString(R.string.display_time, hourOfDay, minute));

        // change context
        Calendar calendar = contextEntity.getCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        contextEntity.setCalendar(calendar);
    }
    @Override
    public void onDatePass(int year, int month, int dayOfMonth) {
        // change textView
        TextView dateView = findViewById(R.id.textDate);
        dateView.setText(getString(R.string.display_date, year, month, dayOfMonth));

        Calendar calendar = contextEntity.getCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        contextEntity.setCalendar(calendar);
    }
}


