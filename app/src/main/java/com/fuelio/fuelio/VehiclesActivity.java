package com.fuelio.fuelio;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fuelio.fuelio.adapters.vehicleAdapter;
import com.fuelio.fuelio.models.vehicle;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

        loadfVehicles();
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

                    vehicle = new vehicle(name, plate, document.getId());
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
