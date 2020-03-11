package com.fuelio.fuelio;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fuelio.fuelio.adapters.serviceAdapter;
import com.fuelio.fuelio.models.service;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class ServicesActivity extends AppCompatActivity {

    SwipeRefreshLayout refresh;
    TextView info;

    RecyclerView recyclerView;
    serviceAdapter cardBusAdapter;
   com.fuelio.fuelio.models.service service;
    List<service> services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setTitle("");
        info = findViewById(R.id.info);

        services = new ArrayList<>();

        refresh = findViewById(R.id.swipe_layout);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        refresh.setOnRefreshListener(() -> {
            refresh.postDelayed(() -> refresh.setRefreshing(false), 3000);
        });


        recyclerView = findViewById(R.id.my_recycler_view);
        cardBusAdapter = new serviceAdapter(services, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(cardBusAdapter);

        listenServices();
    }


    public void listenServices() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading...");

        String myId = new PrefManager(this).getId();

        String st=new PrefManager(getApplicationContext()).getRegistrationToken();

        CollectionReference reference = FirebaseFirestore.getInstance().collection("stations").document(st).collection("services");

        reference.get().addOnSuccessListener(queryDocumentSnapshots -> {

            info.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("name");
                    String amount = document.getString("amount");


                    service = new service(name, amount,document.getId());
                    services.add(service);

                }
                cardBusAdapter.notifyDataSetChanged();
            } else {
                info.setVisibility(View.VISIBLE);
                info.setText("You did not add any service, add them on your home.");
            }

        }).addOnFailureListener(e -> info.setText("Failed to get your services"));

    }


}
