package com.fuelio.fuelio;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fuelio.fuelio.adapters.userStationAdapter;
import com.fuelio.fuelio.models.userStation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class AllProvidersActivity extends AppCompatActivity {


    SwipeRefreshLayout refresh;
    TextView info;

    RecyclerView recyclerView;
    userStationAdapter cardBusAdapter;
   com.fuelio.fuelio.models.userStation userStation;
    List<userStation> userStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        toolbar.setTitle("All providers");
        info = findViewById(R.id.info);

        userStations = new ArrayList<>();

        refresh = findViewById(R.id.swipe_layout);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        refresh.setOnRefreshListener(() -> {
            refresh.postDelayed(() -> refresh.setRefreshing(false), 3000);
        });


        recyclerView = findViewById(R.id.my_recycler_view);
        cardBusAdapter = new userStationAdapter(userStations, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(cardBusAdapter);

        listenProviders();
    }


    public void listenProviders() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading...");

        CollectionReference reference = FirebaseFirestore.getInstance().collection("stations");


        reference.get().addOnSuccessListener(queryDocumentSnapshots -> {

            info.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("name");
                    String address = document.getString("address");
                    String phone=document.getString("phone");
                    String cat=document.getString("category");
                    String id=document.getId();


                    userStation = new userStation(name, address,cat, document.getId());
                    userStations.add(userStation);

                }
                cardBusAdapter.notifyDataSetChanged();
            } else {
                info.setVisibility(View.VISIBLE);
                info.setText("No available providers.");
            }

        }).addOnFailureListener(e -> info.setText("Failed to get all providers"));

    }


}
