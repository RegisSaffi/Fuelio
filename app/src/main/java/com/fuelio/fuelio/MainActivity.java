package com.fuelio.fuelio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks {


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static double latitude;
    public static double longitude;

    String selectedCategory="station";

    MaterialCardView garagecard,stationCard,gStationCard;
    AlertDialog.Builder gpsDialog;
    String myPhone, myId;
    Context mContext;

    Location mLastLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    boolean animated = false;
    Handler handler;
    Runnable runnable;
    BottomSheetDialog dialog;

    Marker marker;
    private GoogleMap mMap;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showNewVehicle());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        myId=new PrefManager(this).getId();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mContext=this;
        showSelectCategory();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public void getStations(String cate) {

        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("stations");

        reference.whereEqualTo("category",cate).get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = (String) document.get("name");
                    String address = (String) document.get("address");

                    String id = (String) document.getId();

                    Map<String, Double> loc1 = (Map<String, Double>) document.get("location");
                    LatLng loc = new LatLng(loc1.get("latitude"), loc1.get("longitude"));

                    LatLng latLng = new LatLng(loc.latitude, loc.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);

                    markerOptions.title(name);
                    markerOptions.snippet(id);

                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                    Marker stationMarker = mMap.addMarker(markerOptions);

                }

            }else{
                findViewById(R.id.progress).setVisibility(View.GONE);
                Toasty.info(mContext,"No stations available in this category yet.",Toasty.LENGTH_LONG).show();
            }

        });
    }

    void showSelectCategory(){

            dialog=new BottomSheetDialog(MainActivity.this);
            View view= LayoutInflater.from(MainActivity.this).inflate(R.layout.select_category_sheet,null);
            dialog.setContentView(view);
            garagecard=view.findViewById(R.id.garage_card);
            stationCard=view.findViewById(R.id.station_card);
            gStationCard=view.findViewById(R.id.gstation_card);

            garagecard.setOnClickListener(v-> {
                selectedCategory="garage";
                getStations("garage");

                dialog.cancel();
            });
        stationCard.setOnClickListener(v-> {
            selectedCategory="station";
            getStations("station");

            dialog.cancel();
        });
        gStationCard.setOnClickListener(v-> {
            selectedCategory="station-garage";
            getStations("station-garage");
            dialog.cancel();
        });

        dialog.setCancelable(false);
            dialog.show();
    }

    void showNewVehicle(){
        dialog=new BottomSheetDialog(MainActivity.this);
        View view= LayoutInflater.from(MainActivity.this).inflate(R.layout.newvehicle_sheet,null);
        dialog.setContentView(view);

        TextInputEditText model,plate,color;
        FancyButton save;
        save=dialog.findViewById(R.id.save);
        model=dialog.findViewById(R.id.model);
        plate=dialog.findViewById(R.id.plate);
        color=dialog.findViewById(R.id.color);

        save.setOnClickListener(v->{

            if(model.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Enter model name").show();
            }else if(plate.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Enter plate number").show();
            }else {

                save.setEnabled(false);
                DocumentReference vRef=FirebaseFirestore.getInstance().collection("vehicles").document();
                Map<String,Object> vMap=new HashMap<>();
                vMap.put("model",model.getText().toString());
                vMap.put("plate",plate.getText().toString());
                vMap.put("color",color.getText().toString());
                vMap.put("owner",myId);

                vRef.set(vMap).addOnSuccessListener(aVoid -> {
                    save.setEnabled(true);
                    Toasty.success(mContext,"Vehicle saved successfully").show();
                    dialog.cancel();
                }).addOnFailureListener(e -> {
                    save.setEnabled(true);
                    Toasty.error(mContext,"Failed").show();
                    dialog.cancel();
                });

            }
        });

        dialog.show();

    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        super.onPause();
    }
    @Override
    protected void onResume() {

        if (!isLocationEnabled(this)) {
            showSettingsAlert();
        }

        super.onResume();
    }

    public void showSettingsAlert() {

        gpsDialog = new AlertDialog.Builder(mContext);

        gpsDialog.setOnCancelListener(dialogInterface -> {

            if (!isLocationEnabled(mContext)) {
                showSettingsAlert();
            }
        });
        // Setting Dialog Title
        gpsDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        gpsDialog.setMessage("GPS is not enabled. Turn on GPS first for the app to function.");

        // On pressing Settings button
        gpsDialog.setPositiveButton("Turn on", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });

        gpsDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        // Showing Alert Message
        gpsDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            return lm.isLocationEnabled();

        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));

            return true;
        }else{

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are u sure you want to logout of your account?")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    }).setPositiveButton("Logout", (dialogInterface, i) -> {
                clearAppData();
                new PrefManager(MainActivity.this).logout();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();


            }).show();

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_account) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));

        } else if (id == R.id.nav_history) {
           startActivity(new Intent(MainActivity.this, HistoryActivity.class));

        } else if (id == R.id.nav_vehicles) {
            startActivity(new Intent(MainActivity.this, VehiclesActivity.class));

        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are u sure you want to logout of your account?")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    }).setPositiveButton("Logout", (dialogInterface, i) -> {
                clearAppData();
                new PrefManager(MainActivity.this).logout();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();


            }).show();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnMyLocationClickListener(location -> {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.snippet("me");
            markerOptions.title(new PrefManager(getApplicationContext()).getName());

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            if (marker != null) {
                marker.remove();
            }

            marker = mMap.addMarker(markerOptions);

            if (!animated) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                animated = true;
            }
        });


        mMap.setOnMarkerClickListener(marker -> {

            marker.hideInfoWindow();
            String name=marker.getTitle();
            String address=marker.getSnippet();
            Intent in=new Intent(getApplicationContext(),StationDetailsActivity.class);
            in.putExtra("name",name);
            in.putExtra("id",marker.getSnippet());
            in.putExtra("latitude",latitude);
            in.putExtra("longitude",longitude);
            startActivity(in);
//
            return true;
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }

        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.snippet("You");
        markerOptions.title(new PrefManager(getApplicationContext()).getName());

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if (marker != null) {
            marker.remove();
        }

        marker = mMap.addMarker(markerOptions);

        if (!animated) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            animated = true;
        }

//        Map<String, Object> loc = new HashMap<>();
//        loc.put("coordinates", new LatLng(location.getLatitude(), location.getLongitude()));
//        loc.put("time", location.getTime());
//
//        FirebaseFirestore.getInstance().collection("users").document(myId).set(loc, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//            }
//        });


    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
