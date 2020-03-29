package com.fuelio.fuelio;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class PendingActivity extends AppCompatActivity {

    TextView timeTv, distanceTv, nameTv, costTv, modelTv, issueTv, statusTv, desc;
    FancyButton cancel;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dialog);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        nameTv = findViewById(R.id.name);
        modelTv = findViewById(R.id.model);
        costTv = findViewById(R.id.cost);
        timeTv = findViewById(R.id.time);
        distanceTv = findViewById(R.id.distance);
        cancel = findViewById(R.id.cancel);
        statusTv = findViewById(R.id.status);
        issueTv = findViewById(R.id.issueTv);
        progressBar = findViewById(R.id.progress);
        desc = findViewById(R.id.desc);

        CollectionReference ref = FirebaseFirestore.getInstance().collection("requests");

        Query refQuery = ref.limit(1).orderBy("modified", Query.Direction.ASCENDING);

        listenPending(refQuery);
    }


    public void listenPending(Query refQuery) {
        progressBar.setVisibility(View.VISIBLE);
        cancel.setEnabled(false);

        refQuery.addSnapshotListener((queryDocumentSnapshots, e) -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                String name = document.getString("station_name");
                String issue = document.getString("issue");
                String dest = document.getString("destination_name");
                String status = document.getString("status");
                String model = document.getString("model");

                Timestamp time = document.getTimestamp("time");
                String distance = document.getString("distance");
                String duration = document.getString("duration");

                timeTv.setText(duration);
                nameTv.setText(name);
                modelTv.setText(model);
                //  toolbar.setSubtitle(time.toDate().toString().substring(0,16));
                distanceTv.setText(distance);
                costTv.setText("0.00");
                issueTv.setText(issue);

                progressBar.setVisibility(View.GONE);
                cancel.setEnabled(true);

                listenSingleOne(document);
            }
        });
    }


    void listenSingleOne(DocumentSnapshot document) {

        DocumentReference ref = FirebaseFirestore.getInstance().collection("requests").document(document.getId());

        ref.addSnapshotListener((documentSnapshot, e) -> {

            String status = documentSnapshot.getString("status");

            if (status != null && status.equals("accepted")) {
                Toasty.success(PendingActivity.this, "Your request was accepted, you can hit payment once the service is done.", Toasty.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    statusTv.setText(Html.fromHtml("<font color='green'>" + status + "</font>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    statusTv.setText(Html.fromHtml("<font color='green'>" + status + "</font>"));
                }

                cancel.setBorderColor(getResources().getColor(R.color.quantum_googgreen));
                cancel.setBackgroundColor(getResources().getColor(R.color.quantum_googgreen));
                cancel.setText("Confirm and pay");

                cancel.setOnClickListener(vv->{

                    BottomSheetDialog dialog=new BottomSheetDialog(PendingActivity.this);
                    View v2= LayoutInflater.from(PendingActivity.this).inflate(R.layout.payment_sheet,null);
                    dialog.setContentView(v2);

                    dialog.show();
                });

//                cancel.setOnClickListener(v -> {
//                    new AlertDialog.Builder(PendingActivity.this)
//                            .setTitle("Confirm")
//                            .setMessage("Confirm your journey complete and provide some feedback")
//                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
//                                dialogInterface.cancel();
//                            }).setPositiveButton("Confirm", (dialogInterface, i) -> {
//
//                        //cancelling request
//
//                        DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(document.getId());
//                        Map<String, Object> d = new HashMap<>();
//                        d.put("status", "confirmed");
//                        d.put("modified", true);
//
//                        reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
//                            Toasty.success(PendingActivity.this, "Journey confirmed, you can give us a feedback later.").show();
//                            startActivity(new Intent(PendingActivity.this, MainActivity.class));
//                            finish();
//
//                        }).addOnFailureListener(e2 -> Toasty.error(PendingActivity.this, "Confirmation failed.").show());
//
//                    }).show();
//                });


            } else if (status != null && status.equals("pending")) {

                Toasty.info(PendingActivity.this, "Your request is pending", Toasty.LENGTH_LONG).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    statusTv.setText(Html.fromHtml("<font color='darkblue'>" + status + " request...</font>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    statusTv.setText(Html.fromHtml("<font color='darkblue'>" + status + " request ...</font>"));
                }

                cancel.setOnClickListener(v -> {
                    new AlertDialog.Builder(PendingActivity.this)
                            .setTitle("Confirm")
                            .setMessage("Would you like to cancel your request?")
                            .setNegativeButton("No", (dialogInterface, i) -> {
                                dialogInterface.cancel();
                            }).setPositiveButton("Yes", (dialogInterface, i) -> {

                        //cancelling request

                        DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(document.getId());
                        Map<String, Object> d = new HashMap<>();
                        d.put("status", "cancelled");
                        d.put("modified", true);

                        reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                            Toasty.success(PendingActivity.this, "Request cancelled by you.").show();
                            startActivity(new Intent(PendingActivity.this, MainActivity.class));
                            finish();

                        }).addOnFailureListener(e2 -> Toasty.error(PendingActivity.this, "Cancel failed.").show());

                    }).show();
                });
            } else if (status != null && status.equals("confirmed")) {

            } else if (status != null && status.equals("cancelled")) {

            } else {
                startActivity(new Intent(PendingActivity.this, MainActivity.class));
                finish();
                Toasty.error(PendingActivity.this, "Your request might have been rejected by the service provider.").show();
            }


        });

    }

}
