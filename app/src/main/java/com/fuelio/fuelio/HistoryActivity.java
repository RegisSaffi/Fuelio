package com.fuelio.fuelio;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fuelio.fuelio.adapters.requestCardsAdapter;
import com.fuelio.fuelio.models.requestCard;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends AppCompatActivity {


    SwipeRefreshLayout refresh;
    TextView info;

    RecyclerView recyclerView;
    requestCardsAdapter cardBusAdapter;
   com.fuelio.fuelio.models.requestCard requestCard;
    List<requestCard> requestCards;

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

        setTitle("");
        info = findViewById(R.id.info);

        requestCards = new ArrayList<>();

        refresh = findViewById(R.id.swipe_layout);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        refresh.setOnRefreshListener(() -> {
            refresh.postDelayed(() -> refresh.setRefreshing(false), 3000);
        });


        recyclerView = findViewById(R.id.my_recycler_view);
        cardBusAdapter = new requestCardsAdapter(requestCards, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(cardBusAdapter);

        listenRequests();
    }


    public void listenRequests() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading...");

        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("requests");
        Query query = reference.whereEqualTo("requester_id", myId);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            info.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("requester_name");
                    String issue = document.getString("issue");
                    Timestamp date=document.getTimestamp("time");
                    String desc=document.getString("description");
                    String id=document.getId();
                    String status=document.getString("status");


                    requestCard = new requestCard(name, issue, date.toDate().toString().substring(0,16), status,desc, document.getId());
                    requestCards.add(requestCard);

                }
                cardBusAdapter.notifyDataSetChanged();
            } else {
                info.setVisibility(View.VISIBLE);
                info.setText("No available histories, make some first.");
            }

        }).addOnFailureListener(e -> info.setText("Failed to get your histories"));

    }


}