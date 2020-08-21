package com.fuelio.fuelio;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fuelio.fuelio.adapters.userStationAdapter;

import com.fuelio.fuelio.models.userStation;
import com.google.firebase.Timestamp;
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


public class AllUsersActivity extends AppCompatActivity {

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

        toolbar.setTitle("All users");
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

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int pos) {
                userStation user=userStations.get(pos);

                new AlertDialog.Builder(AllUsersActivity.this)
                        .setMessage("What do you want to do with this user?")
                        .setTitle("User actions")
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(user.getId());
                                reference.delete();

                                finish();
                            }
                        }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNeutralButton("Disable/Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(user.getId());
                        Map<String, Object> d = new HashMap<>();
                        d.put("disabled", true);
                        reference.set(d, SetOptions.merge());

                        finish();
                    }
                }).show();

            }
            @Override
            public void onLongClick(View view, int pos) {

            }
        }));

        listenRequests();
    }


    public void listenRequests() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading...");

        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("users");


       reference.get().addOnSuccessListener(queryDocumentSnapshots -> {

            info.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String fname = document.getString("first_name");
                    String lname = document.getString("last_name");

                    String phone=document.getString("phone");
                    String id=document.getId();

                    userStation = new userStation(fname+" "+lname,phone,"user", document.getId());
                    userStations.add(userStation);

                }
                cardBusAdapter.notifyDataSetChanged();
            } else {
                info.setVisibility(View.VISIBLE);
                info.setText("No available users, make some first.");
            }

        }).addOnFailureListener(e -> info.setText("Failed to get all users"));

    }


}
