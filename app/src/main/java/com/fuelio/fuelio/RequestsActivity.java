package com.fuelio.fuelio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class RequestsActivity extends AppCompatActivity {

    TextView distanceTv, timeTv, nameTv, phoneTv, seatsTv, issueTv, descTv,dateTv,costTv,providerTv,desc2Tv;

    FancyButton accept, reject;

    MaterialButton call;

    String requestId, issue;
    ProgressBar progress;
    Toolbar toolbar;

    String phn;
    boolean complete,nothing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }


        costTv = findViewById(R.id.cost);
        distanceTv = findViewById(R.id.distance);
        nameTv = findViewById(R.id.name);
        phoneTv = findViewById(R.id.phone);
        seatsTv = findViewById(R.id.seats);
        issueTv = findViewById(R.id.issueTv);
        descTv = findViewById(R.id.desc);
        dateTv=findViewById(R.id.dateTv);
        timeTv = findViewById(R.id.time);
        desc2Tv=findViewById(R.id.desc2);

        providerTv = findViewById(R.id.provider);

        accept = findViewById(R.id.accept);
        reject = findViewById(R.id.reject);
        call = findViewById(R.id.call);
        progress = findViewById(R.id.progress);

        requestId = getIntent().getStringExtra("id");
        issue = getIntent().getStringExtra("origin");
        complete=getIntent().getBooleanExtra("complete",false);
        nothing=getIntent().getBooleanExtra("nothing",false);

        issueTv.setText(issue);

        setTitle("Request details");

        if(complete){
            reject.setVisibility(View.GONE);
            accept.setBackgroundColor(getResources().getColor(R.color.quantum_googgreen));
            accept.setText("Complete request");
        }

        if(nothing){
            reject.setVisibility(View.GONE);
            accept.setVisibility(View.GONE);
        }

        call.setOnClickListener(v -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        return;
                    }
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phn));
                startActivity(callIntent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        accept.setOnClickListener(v -> {

            if(complete){
                makeReport();
            }else {
                show2();
            }

        });

        reject.setOnClickListener(v -> {

           show();

        });
        getRequestDetails();
    }


    public void show2() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(RequestsActivity.this);
        builder.setTitle("Accept request");
        builder.setMessage("You are about to accept this request, so user will be waiting for the service.");

        final EditText input = new EditText(RequestsActivity.this);

        input.setSingleLine(false);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        input.setHint("Write comment");
        input.setMinLines(1);

        builder.setView(input);

        builder.setPositiveButton("Accept", (dialog, which) -> {
            String text = input.getText().toString();

            if (text.equals("")) {
          text="No comment";
           }

            accept.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(requestId);
            Map<String, Object> d = new HashMap<>();
            d.put("status", complete?"completed":"accepted");
            d.put("reason",text);

            reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                Toasty.success(RequestsActivity.this, complete?"The request was completed":"User request accepted,we'll notify user once you reach in his/her area.").show();
                finish();
            }).addOnFailureListener(e -> {
                        Toasty.error(RequestsActivity.this, "Acceptance failed.").show();
                        accept.setEnabled(true);
                        progress.setVisibility(View.GONE);

                    }
            );


        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void show() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(RequestsActivity.this);
        builder.setTitle("Request rejection");
        builder.setMessage("Are you sure you want to reject this request?, please, provide rejection reason for the customer concerns.");

        final EditText input = new EditText(RequestsActivity.this);

        input.setSingleLine(false);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        input.setHint("Write reason");
        input.setMinLines(1);

        builder.setView(input);

        builder.setPositiveButton("Reject", (dialog, which) -> {
            String text = input.getText().toString();

            if (text.equals("")) {
                Toast.makeText(RequestsActivity.this, "Please Enter rejection reason", Toast.LENGTH_SHORT).show();
            } else {

                reject.setEnabled(false);
                progress.setVisibility(View.VISIBLE);
                DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(requestId);
                Map<String, Object> d = new HashMap<>();
                d.put("status", "rejected");
                d.put("reason",text);
                d.put("modified", true);

                reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                    Toasty.info(RequestsActivity.this, "User request rejected").show();
                    finish();
                }).addOnFailureListener(e -> {
                            accept.setEnabled(true);
                            progress.setVisibility(View.GONE);
                            Toasty.error(RequestsActivity.this, "Rejection failed.").show();
                        }
                );

            }

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void makeReport() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(RequestsActivity.this);
        builder.setTitle("Make report");
        builder.setMessage("After service, you might find additional materials were needed for action to complete, or you might need to add other report comment..");

        final EditText input = new EditText(RequestsActivity.this);

        input.setSingleLine(false);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        input.setHint("Write report comment");
        input.setMinLines(1);

        builder.setView(input);

        builder.setPositiveButton("Make report", (dialog, which) -> {
            String text = input.getText().toString();

            if (text.equals("")) {
                text="No report comment";
            }

            accept.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(requestId);
            Map<String, Object> d = new HashMap<>();
            d.put("status","completed");
            d.put("reason",text);

            reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                Toasty.success(RequestsActivity.this, "The request report was completed").show();
                finish();
            }).addOnFailureListener(e -> {
                        Toasty.error(RequestsActivity.this, "Reporting failed.").show();
                        accept.setEnabled(true);
                        progress.setVisibility(View.GONE);

                    }
            );


        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void getRequestDetails() {
        progress.setVisibility(View.VISIBLE);
        call.setEnabled(false);
        accept.setEnabled(false);
        reject.setEnabled(false);

        DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(requestId);
        reference.get().addOnSuccessListener(document -> {

            String name = document.getString("requester_name");
            String desc = document.getString("description");

            String issue = document.getString("issue");
            String phone = document.getString("requester_phone");

            Timestamp time = document.getTimestamp("time");
            String distance = document.getString("distance");
            String amount = document.getString("amount");
            String duration = document.getString("duration");
            String reason = document.getString("reason");

            String st = document.getString("station_name");

            desc2Tv.setText(reason);
            timeTv.setText(duration);
            nameTv.setText(name);
            phoneTv.setText(phone);
            toolbar.setSubtitle(time.toDate().toString().substring(0, 16));
            distanceTv.setText(distance);
            costTv.setText(amount);
            descTv.setText(desc);
            issueTv.setText(issue);

            providerTv.setText(st);

            dateTv.setText(time.toDate().toString());
            progress.setVisibility(View.GONE);
            call.setEnabled(true);
            accept.setEnabled(true);
            reject.setEnabled(true);

        }).addOnFailureListener(e -> {
            progress.setVisibility(View.GONE);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toasty.warning(this, "Please enable call permission", Toast.LENGTH_SHORT).show();
        }
    }
}
