package com.syv.simplermaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.EncodedPolyline;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<DirectionsResult> {

    private static final int DEFAULT_ZOOM = 15;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 432;
    private static final String NAVIGATION_FROM_KEY = "navigation_from_key";
    private static final String NAVIGATION_TO_KEY = "navigation_to_key";
    private GeoApiContext geoApiContext;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private GoogleMap mMap;
    private Button navigateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        initViews();

        navigationView.setNavigationItemSelectedListener(MapsActivity.this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MapsActivity.this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();

        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View navigationView = LayoutInflater.from(MapsActivity.this).inflate(R.layout.layout_navigation_data, null, false);
                Button button = navigationView.findViewById(R.id.layout_navigation_button_submit);

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this).setView(navigationView);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String from = ((EditText) navigationView.findViewById(R.id.layout_navigation_edit_text_from)).getText().toString();
                        String to = ((EditText) navigationView.findViewById(R.id.layout_navigation_edit_text_to)).getText().toString();
                        alertDialog.cancel();
                        Bundle args = new Bundle();
                        args.putString(NAVIGATION_FROM_KEY, from);
                        args.putString(NAVIGATION_TO_KEY, to);
                        LoaderManager loaderManager = getSupportLoaderManager();
                        loaderManager.restartLoader(123, args, MapsActivity.this);
                    }
                });
            }
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.main_drawer_layout);
        navigationView = findViewById(R.id.maps_navigation_view);
        navigateButton = findViewById(R.id.main_button_navigate);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (getLocationPermission()) {
            updateLocation();
            focusCurrentLocation();
        }

    }

    private void addMarkersToMap(DirectionsResult results) {
        if (results.routes.length > 0) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].startLocation.lat, results.routes[0].legs[0].startLocation.lng)).title(results.routes[0].legs[0].startAddress));
            mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].endLocation.lat, results.routes[0].legs[0].endLocation.lng)).title(results.routes[0].legs[0].startAddress).snippet(getEndLocationTitle(results)));
            PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            for(int i=0; i<results.routes[0].legs[0].steps.length; i++) {
//                EncodedPolyline polyline = results.routes[0].legs[0].steps[0].polyline;
//                TODO: Draw Route on Map.
            }

        }
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable + " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }


    private boolean getLocationPermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                    focusCurrentLocation();
                }
                break;

        }
    }


    private void updateLocation() {
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
            // TODO: verify if the control will ever come here.
        }
    }

    private void focusCurrentLocation() {
        try {
            Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
            locationTask.addOnCompleteListener(MapsActivity.this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        //TODO: Handle the camera focus  if no location available.
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.maps_menu_item_home:
                return true;
            case R.id.maps_menu_item_exit:
                finish();
                // TODO: Original way to exit an app.
                return true;
        }
        return false;
    }

    @Override
    public Loader<DirectionsResult> onCreateLoader(int id, Bundle args) {
        return new DirectionResultLoader(MapsActivity.this, geoApiContext, args.getString(NAVIGATION_FROM_KEY), args.getString(NAVIGATION_TO_KEY));
    }

    @Override
    public void onLoadFinished(Loader<DirectionsResult> loader, DirectionsResult data) {
        addMarkersToMap(data);

    }

    @Override
    public void onLoaderReset(Loader<DirectionsResult> loader) {

    }
}