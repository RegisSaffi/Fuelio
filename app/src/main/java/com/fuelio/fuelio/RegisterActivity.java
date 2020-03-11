package com.fuelio.fuelio;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.dpro.widgets.OnWeekdaysChangeListener;
import com.dpro.widgets.WeekdaysPicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.schibstedspain.leku.LocationPickerActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.schibstedspain.leku.LocationPickerActivityKt.LATITUDE;
import static com.schibstedspain.leku.LocationPickerActivityKt.LOCATION_ADDRESS;
import static com.schibstedspain.leku.LocationPickerActivityKt.LONGITUDE;


public class RegisterActivity extends AppCompatActivity {

    FancyButton register;

    TextInputEditText fname,lname,email,phone,name,website,desc,address;

    ProgressDialog progress;

    String selectedGender="Male";
    String phoneNumber;
    SwitchCompat driver;

    boolean isStation=false;
    LatLng coordinates;

    LinearLayout driveLayout;
    WeekdaysPicker widget;
    AppCompatSpinner category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fname=findViewById(R.id.fname);
        lname=findViewById(R.id.lname);
        email=findViewById(R.id.email);
        phone=findViewById(R.id.phone);

        register=findViewById(R.id.btnRegister);

        driveLayout=findViewById(R.id.driveLayout);
        name=findViewById(R.id.name);
        website=findViewById(R.id.website);
        desc=findViewById(R.id.desc);
        address=findViewById(R.id.address);
        category=findViewById(R.id.category);


        widget =  findViewById(R.id.weekdays);
        widget.setOnWeekdaysChangeListener((view, clickedDayOfWeek, selectedDays) -> {
            // Do Something
        });

        driver=findViewById(R.id.driver);

        phoneNumber=getIntent().getStringExtra("phone");
        phone.setText(phoneNumber);


        driver.setOnCheckedChangeListener((compoundButton, b) -> {

            isStation = b;

            if(b){
                driveLayout.setVisibility(View.VISIBLE);
            }else{
                driveLayout.setVisibility(View.GONE);
            }
        });

        register.setOnClickListener(v->validate());

        address.setOnClickListener(v-> getLocation());

    }



    void getLocation(){

        LocationPickerActivity.Builder builder= new LocationPickerActivity.Builder()
                //.withLocation(41.4036299, 2.1743558)
                .withVoiceSearchHidden()
                .withGoogleTimeZoneEnabled()
                .withGeolocApiKey(getResources().getString(R.string.google_api_key))
                .withGooglePlacesEnabled()
                .withGoogleTimeZoneEnabled();
        startActivityForResult(builder.build(this), 222);
    }

    void validate(){
        if(fname.getText().toString().equals("")){
            Toasty.error(getApplicationContext(),"First name is empty").show();
        }else if(lname.getText().toString().equals("")){
            Toasty.error(getApplicationContext(),"Last name is empty").show();
        }else if(phone.getText().toString().equals("")){
            Toasty.error(getApplicationContext(),"First name is empty").show();
        }else if(isStation){
            if(name.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Since you are a station, enter station name.").show();
            }else if(desc.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Enter station description").show();
            }else if(address.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Click address to select station location").show();
            }else if(coordinates==null){
                Toasty.error(getApplicationContext(),"Please click address to select location").show();
            }else if(widget.noDaySelected()){
                Toasty.error(getApplicationContext(),"Select your working days").show();
            }else if(category.getSelectedItem().toString().equals("Choose station category")){
                Toasty.error(getApplicationContext(),"Choose the category").show();
            }else{
                register();
            }
        }
        else{

            register();
        }
    }


    void register(){
        progress=ProgressDialog.show(RegisterActivity.this,"","Please wait...",false,false);

        CollectionReference userRef= FirebaseFirestore.getInstance().collection("users");
        DocumentReference id=userRef.document();

        Map<String,Object> user=new HashMap<>();
        user.put("first_name",fname.getText().toString());
        user.put("last_name",lname.getText().toString());
        user.put("phone",phoneNumber);
        user.put("email",email.getText().toString());
        user.put("type",isStation?"station":"user");

        if(isStation){
            Map<String,Object> userStation=new HashMap<>();
            userStation.put("name",name.getText().toString());
            userStation.put("website",website.getText().toString());
            userStation.put("description",desc.getText().toString());
            userStation.put("location",coordinates);
            userStation.put("address",address.getText().toString());
            userStation.put("days",widget.getSelectedDaysText());
            userStation.put("phone",phoneNumber);
            userStation.put("category",category.getSelectedItem().toString());
            userStation.put("owner",id.getId());

            DocumentReference stationRef=FirebaseFirestore.getInstance().collection("stations").document();
            user.put("station",stationRef.getId());

            stationRef.set(userStation).addOnSuccessListener(aVoid -> {

            });

            new PrefManager(getApplicationContext()).setType("station");
            new PrefManager(getApplicationContext()).setAbout(name.getText().toString());
            new PrefManager(getApplicationContext()).setRegistrationtoken(stationRef.getId());
        }else{
            new PrefManager(getApplicationContext()).setType("user");
        }

        userRef.document(id.getId()).set(user).addOnSuccessListener(aVoid -> {

            progress.cancel();

            Toasty.success(getApplicationContext(),"Registered successfully").show();

            new PrefManager(getApplicationContext()).saveUser(id.getId(),fname.getText().toString()+" "+lname.getText().toString());
            new PrefManager(getApplicationContext()).setFirstTimeLaunch(false);

            Intent in;
            if(isStation) {
                new PrefManager(getApplicationContext()).setType("station");
                in = new Intent(getApplicationContext(), StationActivity.class);
            }else{
                in = new Intent(getApplicationContext(), MainActivity.class);
            }

            in.putExtra("user",true);
            in.putExtra("phone",phoneNumber.replace("+",""));

            startActivity(in);
            finish();

        }).addOnFailureListener(e -> Toasty.error(getApplicationContext(),"Registration failed, try again.").show())
                .addOnCompleteListener(task -> progress.cancel());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 222 && resultCode==RESULT_OK) {
            double latitude = data.getDoubleExtra(LATITUDE, 0.0);

            double longitude = data.getDoubleExtra(LONGITUDE, 0.0);
            String name = data.getStringExtra(LOCATION_ADDRESS);
            coordinates = new LatLng(latitude,longitude);

            address.setText(name);

        }
    }
}
