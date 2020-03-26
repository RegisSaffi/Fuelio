package com.fuelio.fuelio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelio.fuelio.adapters.reviewAdapter;
import com.fuelio.fuelio.adapters.vehicleAdapter;
import com.fuelio.fuelio.models.review;
import com.fuelio.fuelio.models.vehicle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

public class StationDetailsActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static String API;
 public static String directionUrl = "https://maps.googleapis.com/maps/api/directions/json?";
    public static double latitude;
    public static double longitude;
    public int count2 = 0;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Context mContext;
    ImageView toggle;
    Marker marker;
    BottomSheetDialog dialog;
    BottomSheetBehavior sheetBehavior;
    double originLatitude, originLongitude, destinationLatitude, destinationLongitude;
    String destinationName, destinationId;

    TextView timeTv, distanceTv, nameTv,descTv,emailTv,phoneTv,webTv;
    ProgressDialog progressDialog;

    FancyButton requestBtn,preButton;
    DocumentSnapshot stationDocument;
    AppCompatSpinner spinner;
    TextInputEditText more;

    RecyclerView recyclerView;
    vehicleAdapter cardBusAdapter;
    com.fuelio.fuelio.models.vehicle vehicle;
    List<vehicle> vehicles;
    ImageView review;

    private boolean bound = false;

    Toolbar toolbar;
    String[] garageServices;
    String[] stationServices;
    String[] allServices;

    RecyclerView recyclerView2;
    reviewAdapter reviewAdapter;
    List<com.fuelio.fuelio.models.review> reviews;
    TextView info;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationdetails);

        API = "AIzaSyB3oJj1ebrokzm6T0nbtyBhSljHf4OkBHs";

        Bundle bundle = getIntent().getExtras();

        garageServices=getResources().getStringArray(R.array.garage);
        stationServices=getResources().getStringArray(R.array.station);
        allServices=getResources().getStringArray(R.array.all_services);

        originLatitude = bundle.getDouble("latitude", latitude);
        originLongitude = bundle.getDouble("longitude", longitude);
        
        destinationName = bundle.getString("name");
        destinationId = bundle.getString("id");
    
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(destinationName);

        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        nameTv = findViewById(R.id.tvName);
        timeTv = findViewById(R.id.tvTime);
        distanceTv = findViewById(R.id.tvDistance);
        webTv=findViewById(R.id.tvWeb);
        emailTv=findViewById(R.id.tvEmail);
        descTv=findViewById(R.id.tvDesc);
        phoneTv=findViewById(R.id.tvPhone);
        more=findViewById(R.id.more);
        spinner=findViewById(R.id.issue);
        review=findViewById(R.id.review);
        review.setOnClickListener(v->showAddReview());

        requestBtn = findViewById(R.id.request);
        preButton=findViewById(R.id.request2);

        preButton.setOnClickListener(v->toggleBottomSheet());
        toggle = findViewById(R.id.toggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }


        vehicles = new ArrayList<>();
        reviews=new ArrayList<>();

        info=findViewById(R.id.info);

        recyclerView = findViewById(R.id.my_recycler_view);
        cardBusAdapter = new vehicleAdapter(vehicles, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(cardBusAdapter);

        recyclerView2 = findViewById(R.id.my_recycler_view2);
        reviewAdapter = new reviewAdapter(reviews, this);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView2.setAdapter(reviewAdapter);

        setTitle(destinationName);
        loadfVehicles();

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {

                        toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_down_black_24dp, getTheme()));

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {

                        toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_up_black_24dp, getTheme()));

                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        
        mContext = this;

        toggle.setOnClickListener(v -> toggleBottomSheet());
        requestBtn.setOnClickListener(view -> {

            vehicle selected = null;

            for(int a=0;a<vehicles.size();a++){
                if(vehicles.get(a).getSelected()){
                    selected=vehicles.get(a);
                }
            }

            if(selected==null){
                Toasty.error(getApplicationContext(),"Select one vehicle to continue").show();
                return;
            }else if(more.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Add more description for your issue").show();
                return;
            }else if(spinner.getSelectedItem().toString().equals("Choose service")){
                Toasty.error(getApplicationContext(),"Choose the service you want").show();
                return;
            }

            new AlertDialog.Builder(StationDetailsActivity.this)
                    .setTitle("Send request")
                    .setMessage("Would you like to request your vehicle to be repaired or you are out of gaz?")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {

                    }).setPositiveButton("Request", (dialogInterface, i) -> {
                        requestBtn.setEnabled(false);
                        Map<String, Object> request = new HashMap<>();
                        request.put("requester_id", new PrefManager(getApplicationContext()).getId());
                        request.put("requester_phone", new PrefManager(getApplicationContext()).getPhone());
                        request.put("requester_name", new PrefManager(getApplicationContext()).getName());
                        request.put("requester_location", new LatLng(originLatitude, originLongitude));
                        request.put("time", new Date());
                        request.put("duration",timeTv.getText().toString());
                        request.put("distance",distanceTv.getText().toString());
                        request.put("station_name", stationDocument.getString("name"));
                        request.put("station_id", stationDocument.getId());
                        request.put("modified", false);
                        request.put("issue",spinner.getSelectedItem().toString());
                        request.put("description",more.getText().toString());
                        request.put("status", "pending");

                        FirebaseFirestore.getInstance().collection("requests").document().set(request).addOnSuccessListener(aVoid -> {

                            Toasty.success(getApplicationContext(), "Request sent, you will be notified once request accepted.").show();
                            finish();

                        }).addOnFailureListener(e -> {
                                    Toasty.error(getApplicationContext(), "Error requesting,try again later").show();
                                    requestBtn.setEnabled(true);
                                }
                        );

                    }).setCancelable(false).show();

        });

       getStation();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int pos) {

                for(int a=0;a<vehicles.size();a++){
                    if(a!=pos) {
                        vehicle vehicle1 = vehicles.get(pos);
                        vehicle1.setSelected(false);
                    }
                }

               vehicle vehicle =vehicles.get(pos);
               vehicle.setSelected(!vehicle.getSelected());
               vehicles.set(pos,vehicle);
               cardBusAdapter.notifyDataSetChanged();
            }
            @Override
            public void onLongClick(View view, int pos) {

            }
        }));
    }

    void toggleBottomSheet() {

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_down_black_24dp, getTheme()));

            return;
        }

        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_up_black_24dp, getTheme()));

    }


    void showAddReview(){
        BottomSheetDialog sheetDialog=new BottomSheetDialog(StationDetailsActivity.this);
        View view= LayoutInflater.from(StationDetailsActivity.this).inflate(R.layout.newreview_sheet,null);
        sheetDialog.setContentView(view);

        AppCompatRatingBar ratingBar=view.findViewById(R.id.rating);
        TextInputEditText rev=view.findViewById(R.id.reviewTxt);
        FancyButton save=view.findViewById(R.id.save);

        save.setOnClickListener(v->{

            if(rev.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Enter review").show();
            }else if(ratingBar.getRating()==0){
                Toasty.error(getApplicationContext(),"Give some rating by pressing stars").show();
            }else {

                String myNm=new PrefManager(getApplicationContext()).getName();
                String myId=new PrefManager(getApplicationContext()).getId();

                save.setEnabled(false);
                CollectionReference vRef=FirebaseFirestore.getInstance().collection("reviews");

                Map<String,Object> vMap=new HashMap<>();
                vMap.put("name",myNm);
                vMap.put("id",myId);
                vMap.put("station",destinationId);
                vMap.put("review",rev.getText().toString());
                vMap.put("rating",ratingBar.getRating());

                vRef.document().set(vMap).addOnSuccessListener(aVoid -> {
                    save.setEnabled(true);
                    Toasty.success(mContext,"Review saved successfully").show();
                    sheetDialog.cancel();
                }).addOnFailureListener(e -> {
                    save.setEnabled(true);
                    Toasty.error(mContext,"Failed to save review").show();
                    sheetDialog.cancel();
                });
            }
        });

        sheetDialog.show();

        listenReviews();
    }

    public void loadfVehicles() {

        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("vehicles");
        Query query = reference.whereEqualTo("owner", myId);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("model");
                    String plate = document.getString("plate");

                    vehicle = new vehicle(name, plate, document.getId());
                    vehicle.setIsSmall(true);
                    vehicles.add(vehicle);
                }

                cardBusAdapter.notifyDataSetChanged();
            } else {

                Toasty.info(getApplicationContext(),"You don't have any vehicle, add it first to continue").show();
                finish();
            }

        }).addOnFailureListener(e ->{});

    }

    public void listenReviews() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading reviews...");

        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("reviews");
        Query query = reference.whereEqualTo("station", destinationId);

        query.addSnapshotListener((queryDocumentSnapshots, e) -> {

            info.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("name");
                    String reviewt = document.getString("review");
                    long rating=document.getLong("rating");

                    review review1 = new review(name, reviewt, document.getId(),(int)rating);
                    reviews.add(review1);
                }

                reviewAdapter.notifyDataSetChanged();
            } else {
                info.setVisibility(View.VISIBLE);
                info.setText("No available reviews.");
            }


        });



    }


    void getStation() {

        progressDialog = ProgressDialog.show(StationDetailsActivity.this, "", "Please wait...", true, false);

        requestBtn.setEnabled(false);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("stations").document(destinationId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            requestBtn.setEnabled(true);
            if (documentSnapshot.exists()) {
                stationDocument = documentSnapshot;

                String name = (String) stationDocument.get("name");
                String website = (String) stationDocument.get("website");
                String phone = (String) stationDocument.get("phone");
                String description = (String) stationDocument.get("description");
                String email= (String) stationDocument.get("email");
                String address= (String) stationDocument.get("address");
                String cat= (String) stationDocument.get("category");

                Map<String, Double> loc1 = (Map<String, Double>) stationDocument.get("location");
                LatLng loc = new LatLng(loc1.get("latitude"), loc1.get("longitude"));

                List<String> days = (List<String>) stationDocument.get("days");
List<String> services=new ArrayList<>();
if(stationDocument.contains("services")){

}


                toolbar.setSubtitle(address);
                nameTv.setText(name+"("+cat+")");
                webTv.setText(website);
                descTv.setText(description);
                phoneTv.setText(phone);
                emailTv.setText(email);
               destinationLatitude = loc.latitude;
                destinationLongitude = loc.longitude;

                toolbar.setSubtitle(cat);
                String dirUrl = directionUrl + "origin=" + originLatitude + "," + originLongitude + "&destination=" + destinationLatitude + "," + destinationLongitude + "&key=" + API;

                Log.w("URL: ",dirUrl);

                getDirection gd = new getDirection();
                gd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dirUrl);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
                String[] myServices;
                if(cat.equals("station")){
                    myServices=stationServices;
                    adapter.addAll(stationServices);
                }else if(cat.equals("garage")){
                    myServices=garageServices;
                    adapter.addAll(garageServices);
                }else{
                    myServices=allServices;
                    adapter.addAll(allServices);
                }

              spinner.setAdapter(adapter);
                progressDialog.cancel();

            } else {

                new AlertDialog.Builder(StationDetailsActivity.this)
                        .setTitle("Error")
                        .setMessage("The station is no longer exists.")
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {

                            finish();

                        }).setCancelable(false).show();
            }
        }).addOnFailureListener(e -> {

            new AlertDialog.Builder(StationDetailsActivity.this)
                    .setTitle("Error")
                    .setMessage("Error occurred while loading the requested station,try again later.")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {

                        finish();

                    }).setCancelable(false).show();
                progressDialog.cancel();
                requestBtn.setEnabled(true);
            });


        }

   

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new Builder(this)
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
        markerOptions.snippet("me");
        markerOptions.title(new PrefManager(getApplicationContext()).getName());

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if (marker != null) {
            marker.remove();
        }


        Map<String, Object> loc = new HashMap<>();
        loc.put("coordinates", new LatLng(location.getLatitude(), location.getLongitude()));
        loc.put("time", location.getTime());


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
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(StationDetailsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
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

    @Override
    public void onBackPressed() {

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    ////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////


    // Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    public void decodeDirection(String json) {
        try {
            JSONObject full = new JSONObject(json);

            JSONArray routesArray = full.getJSONArray("routes");
            JSONObject routes = routesArray.getJSONObject(0);
            JSONObject overviewPoly = routes.getJSONObject("overview_polyline");

            JSONArray legs = routes.getJSONArray("legs");
            JSONObject legs0 = legs.getJSONObject(0);
            JSONObject distance = legs0.getJSONObject("distance");
            JSONObject duration = legs0.getJSONObject("duration");

            String distancetxt = distance.getString("text");
            String durationtxt = duration.getString("text");

            distanceTv.setText(distancetxt);
            timeTv.setText(durationtxt);

            String encodedString = overviewPoly.getString("points");
            

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

   
    
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current", strReturnedAddress.toString());
            } else {
                Log.w("My Current", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current", "Canont get Address!");
        }
        return strAdd;
    }

  

    public class getDirection extends AsyncTask<String, Void, String> {
        String server_response = "none";

        @Override
        protected void onPreExecute() {

            findViewById(R.id.progress).setVisibility(View.VISIBLE);
           // TODO: Implement this method
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = readStream(urlConnection.getInputStream());
                    Log.e("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            findViewById(R.id.progress).setVisibility(View.GONE);
           // Toast.makeText(StationDetailsActivity.this, "Response: "+s, Toast.LENGTH_LONG).show();

            if (!(server_response.equals(null) || server_response.equals("none"))) {
                decodeDirection(server_response);

            }
        }
    }
}
