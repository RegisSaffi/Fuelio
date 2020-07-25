package com.fuelio.fuelio;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class PendingActivity extends AppCompatActivity {

    TextView timeTv, distanceTv, nameTv, costTv, modelTv, issueTv, statusTv, desc;
    FancyButton cancel;
    ProgressBar progressBar;

    String servicePrice="0.00";
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

                servicePrice = document.getString("amount");

                timeTv.setText(duration);
                nameTv.setText(name);
                modelTv.setText(model);
                //  toolbar.setSubtitle(time.toDate().toString().substring(0,16));
                distanceTv.setText(distance);
                costTv.setText(servicePrice);
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

                    TextInputEditText amount,phone;
                    FancyButton pay;
                    amount=dialog.findViewById(R.id.amount);
                    phone=dialog.findViewById(R.id.phone);
                    pay=dialog.findViewById(R.id.pay);

                    amount.setText(servicePrice);

                    phone.setText(new PrefManager(PendingActivity.this).getPhone());

                    pay.setOnClickListener(v->{
                        if(amount.getText().toString().equals("")){
                            Toasty.error(PendingActivity.this,"Enter amount").show();
                        }
                        else if(phone.getText().toString().equals("")){
                            Toasty.error(PendingActivity.this,"Enter phone number to pay").show();
                        }else{
                            pay(phone.getText().toString(),amount.getText().toString(),document.getId());
                        }
                    });

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

            }else if(status!=null && status.equals("paid")){
                startActivity(new Intent(PendingActivity.this, MainActivity.class));
                finish();
                //Toasty.error(PendingActivity.this, "Your request paid successfully.").show();
            } else {
                startActivity(new Intent(PendingActivity.this, MainActivity.class));
                finish();
                Toasty.error(PendingActivity.this, "Your request might have been rejected by the service provider.").show();
            }


        });

    }

    void pay(String number,String amount,String id1){

        int rand= new Random(100).nextInt(999999);

        ProgressDialog progress=ProgressDialog.show(PendingActivity.this,"","Initiating payment...",true,false);
        JsonObjectRequest request= new JsonObjectRequest(
                Request.Method.POST,
                "http://akokanya.com/mtn-pay?amount="+amount+"&phone="+number+"&company_name=Fuelio&payment_code="+rand,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progress.cancel();

                        try {
                            String id=response.getString("transactionid");
                            //Toasty.info(PendingActivity.this,response.getString("status") ).show();
                           // Toasty.success(PendingActivity.this,"Service paid successfully").show();

                            DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(id1);
                            Map<String, Object> d = new HashMap<>();
                            d.put("status", "paid");
                            d.put("modified", true);

                            reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {

                                Toasty.success(PendingActivity.this,"Service paid successfully").show();

                                startActivity(new Intent(PendingActivity.this, MainActivity.class));
                                finish();

                            }).addOnFailureListener(e2 -> Toasty.error(PendingActivity.this, "Payment failed.").show());


                        }catch (Exception e){
                            Toasty.error(PendingActivity.this,"Received data error").show();

                            e.printStackTrace();
                        }

                       // Toasty.info(PendingActivity.this,response.toString()).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.cancel();
                error.printStackTrace();
                if(error.networkResponse!=null){
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data));
                    Toasty.info(PendingActivity.this,obj.getString("error") ).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
                }else {

                    Toasty.info(PendingActivity.this, "Payment failed, try again later").show();
                }
            }
        }
        );

        Volley.newRequestQueue(PendingActivity.this).add(request);
    }

}
