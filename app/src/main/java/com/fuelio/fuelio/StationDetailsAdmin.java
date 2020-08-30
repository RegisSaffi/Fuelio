package com.fuelio.fuelio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

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

public class StationDetailsAdmin extends AppCompatActivity {


    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Context mContext;
    ImageView toggle;
    Marker marker;

    double originLatitude, originLongitude, destinationLatitude, destinationLongitude;
    String destinationName, destinationId;

    TextView timeTv, distanceTv, nameTv,descTv,emailTv,phoneTv,webTv;
    ProgressDialog progressDialog;

    FancyButton  preButton,blockButton,reportButton;
    DocumentSnapshot stationDocument;

    TextInputEditText more;

    ImageView review;
    boolean blocked;

    String stId;


    private boolean bound = false;

    Toolbar toolbar;
    String[] garageServices;
    String[] stationServices;
    String[] allServices;

    RecyclerView recyclerView2;
    reviewAdapter reviewAdapter;
    List<com.fuelio.fuelio.models.review> reviews;
    TextView info;

    vehicle myVehicle;

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
        setContentView(R.layout.admin_stationdetails);

        Bundle bundle = getIntent().getExtras();

        garageServices=getResources().getStringArray(R.array.garage);
        stationServices=getResources().getStringArray(R.array.station);
        allServices=getResources().getStringArray(R.array.all_services);

        destinationName = bundle.getString("name");
        destinationId = bundle.getString("id");
    
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(destinationName);


        nameTv = findViewById(R.id.tvName);
        timeTv = findViewById(R.id.tvTime);
        distanceTv = findViewById(R.id.tvDistance);
        webTv=findViewById(R.id.tvWeb);
        emailTv=findViewById(R.id.tvEmail);
        descTv=findViewById(R.id.tvDesc);
        phoneTv=findViewById(R.id.tvPhone);
        more=findViewById(R.id.more);

        review=findViewById(R.id.review);
        review.setOnClickListener(v->showAddReview());

        preButton=findViewById(R.id.request2);
        blockButton=findViewById(R.id.block);

        reportButton=findViewById(R.id.report);

        //preButton.setOnClickListener(v->toggleBottomSheet());

        toggle = findViewById(R.id.toggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }


        reviews=new ArrayList<>();

        info=findViewById(R.id.info);


        recyclerView2 = findViewById(R.id.my_recycler_view2);
        reviewAdapter = new reviewAdapter(reviews, this);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView2.setAdapter(reviewAdapter);


        setTitle(destinationName);
       // loadfVehicles();

        mContext = this;

       getStation();

       preButton.setOnClickListener(v->{

           new AlertDialog.Builder(StationDetailsAdmin.this)
                   .setTitle("Delete")
                   .setMessage("Are u sure you want to delete this service provider? this action can not be undone.")
                   .setNegativeButton("Cancel", (dialogInterface, i) -> {
                       dialogInterface.cancel();
                   }).setPositiveButton("Delete", (dialogInterface, i) -> {


               DocumentReference docRef = FirebaseFirestore.getInstance().collection("stations").document(destinationId);
               docRef.delete();
               finish();


           }).show();

       });

       blockButton.setOnClickListener(v->{

           if(blocked){
               DocumentReference docRef = FirebaseFirestore.getInstance().collection("stations").document(destinationId);
               Map<String, Object> d = new HashMap<>();
               d.put("blocked", false);

               docRef.set(d, SetOptions.merge());
               Toasty.success(StationDetailsAdmin.this,"Blocked successfully").show();
               blockButton.setText("Unblock");
               blocked=false;
               return;
           }


           new AlertDialog.Builder(StationDetailsAdmin.this)
                   .setTitle("Block")
                   .setMessage("Are u sure you want to block this service provider?")
                   .setNegativeButton("Cancel", (dialogInterface, i) -> {
                       dialogInterface.cancel();
                   }).setPositiveButton("Block", (dialogInterface, i) -> {

               DocumentReference docRef = FirebaseFirestore.getInstance().collection("stations").document(destinationId);
               Map<String, Object> d = new HashMap<>();
               d.put("blocked", true);

               docRef.set(d, SetOptions.merge());
               Toasty.info(StationDetailsAdmin.this,"Blocked successfully").show();
               blocked=true;
               blockButton.setText("Unblock");

           }).show();
        });

        reportButton.setOnClickListener(v->{

            Intent in=new Intent(StationDetailsAdmin.this,StationCompleteActivity.class);
            in.putExtra("isme",false);
            in.putExtra("id",stId);

            startActivity(in);

        });
        /////////////////

    }




    void showAddReview(){
        BottomSheetDialog sheetDialog=new BottomSheetDialog(StationDetailsAdmin.this);
        View view= LayoutInflater.from(StationDetailsAdmin.this).inflate(R.layout.newreview_sheet,null);
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

        progressDialog = ProgressDialog.show(StationDetailsAdmin.this, "", "Please wait...", true, false);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("stations").document(destinationId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()) {
                stationDocument = documentSnapshot;

                String name = (String) stationDocument.get("name");
                String website = (String) stationDocument.get("website");
                String phone = (String) stationDocument.get("phone");
                String description = (String) stationDocument.get("description");
                String email= (String) stationDocument.get("email");
                String address= (String) stationDocument.get("address");
                String cat= (String) stationDocument.get("category");

                stId=stationDocument.getId();

                if(stationDocument.contains("blocked")){
                    blocked=stationDocument.getBoolean("blocked");
                    if(blocked){
                        blockButton.setText("Unblock");
                    }else{
                        blockButton.setText("Block");
                    }
                }

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


//                for(String s:myServices){
//
//                    vehicle3=new vehicle(s,"2000","12");
//                    vehicle3.setIsSmall(true);
//                    vehicle3.setIsService(true);
//                    vehicles3.add(vehicle3);
//
//
//                }
//                cardBusAdapter3.notifyDataSetChanged();

                progressDialog.cancel();

            } else {

                new AlertDialog.Builder(StationDetailsAdmin.this)
                        .setTitle("Error")
                        .setMessage("The station is no longer exists.")
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {

                            finish();

                        }).setCancelable(false).show();
            }
        }).addOnFailureListener(e -> {

            new AlertDialog.Builder(StationDetailsAdmin.this)
                    .setTitle("Error")
                    .setMessage("Error occurred while loading the requested station,try again later.")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {

                        finish();

                    }).setCancelable(false).show();
                progressDialog.cancel();

            });


        }




    @Override
    public void onBackPressed() {


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
