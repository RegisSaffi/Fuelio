package com.fuelio.fuelio;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fuelio.fuelio.adapters.vehicleAdapter;
import com.fuelio.fuelio.models.userStation;
import com.fuelio.fuelio.models.vehicle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class VehiclesActivity extends AppCompatActivity {

    SwipeRefreshLayout refresh;
    TextView info;

    RecyclerView recyclerView;
    vehicleAdapter cardBusAdapter;
    vehicle vehicle;
    List<vehicle> vehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setTitle("");
        info = findViewById(R.id.info);

        vehicles = new ArrayList<>();

        refresh = findViewById(R.id.swipe_layout);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        refresh.setOnRefreshListener(() -> {
            refresh.postDelayed(() -> refresh.setRefreshing(false), 3000);
        });


        recyclerView = findViewById(R.id.my_recycler_view);
        cardBusAdapter = new vehicleAdapter(vehicles, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(cardBusAdapter);


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int pos) {

                vehicle v=vehicles.get(pos);

                  showNewVehicle(v);
            }
            @Override
            public void onLongClick(View view, int pos) {

            }
        }));

        loadfVehicles();
    }


    void showNewVehicle(vehicle v1){
        BottomSheetDialog dialog;

        dialog=new BottomSheetDialog(VehiclesActivity.this);
        View view= LayoutInflater.from(VehiclesActivity.this).inflate(R.layout.newvehicle_sheet,null);
        dialog.setContentView(view);

        TextInputEditText model,plate,color;
        FancyButton save;
        save=dialog.findViewById(R.id.save);
        model=dialog.findViewById(R.id.model);
        plate=dialog.findViewById(R.id.plate);
        color=dialog.findViewById(R.id.color);

        model.setText(v1.getName());
        plate.setText(v1.getPlate());
        color.setText(v1.getColor());

        save.setOnClickListener(v->{

            if(model.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Enter model name").show();
            }else if(plate.getText().toString().equals("")){
                Toasty.error(getApplicationContext(),"Enter plate number").show();
            }else {

                save.setText("Edit vehicle");
                save.setEnabled(false);

                DocumentReference vRef=FirebaseFirestore.getInstance().collection("vehicles").document(v1.getId());

                Map<String,Object> vMap=new HashMap<>();
                vMap.put("model",model.getText().toString());
                vMap.put("plate",plate.getText().toString());
                vMap.put("color",color.getText().toString());
                vMap.put("owner",v1.getOwner());

                vRef.set(vMap, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                    save.setEnabled(true);
                    Toasty.success(VehiclesActivity.this,"Vehicle saved successfully").show();
                    dialog.cancel();
                }).addOnFailureListener(e -> {
                    save.setEnabled(true);
                    Toasty.error(VehiclesActivity.this,"Failed").show();
                    dialog.cancel();
                });

            }
        });

        dialog.show();

    }


    public void loadfVehicles() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading...");

        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("vehicles");
        Query query = reference.whereEqualTo("owner", myId);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            info.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("model");
                    String plate = document.getString("plate");
                    String color = document.getString("color");

                    String owner = document.getString("owner");
                    vehicle = new vehicle(name, plate, document.getId());
                    vehicle.setColor(color);
                    vehicle.setOwner(owner);

                    vehicles.add(vehicle);

                }
                cardBusAdapter.notifyDataSetChanged();
            } else {
                info.setVisibility(View.VISIBLE);
                info.setText("No available vehicles, add them first.");
            }

        }).addOnFailureListener(e -> info.setText("Failed to get your vehicles"));

    }


}
