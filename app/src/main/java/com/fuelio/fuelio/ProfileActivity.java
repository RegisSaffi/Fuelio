package com.fuelio.fuelio;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class ProfileActivity extends AppCompatActivity {

    FancyButton delete;
    EditText name;

    ProgressDialog progress;
    TextView nameTv, typeTv, tvName, tvEmail, tvPhone;

 ImageView edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setTitle("");

        delete = findViewById(R.id.delete);
        nameTv = findViewById(R.id.nameTv);
        typeTv = findViewById(R.id.typeTv);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);

        tvName.setText(new PrefManager(this).getName());
        tvEmail.setText(new PrefManager(this).getAbout());
        tvPhone.setText(new PrefManager(this).getPhone());

        edit=findViewById(R.id.edit);

        edit.setImageDrawable(VectorDrawableCompat.create(ProfileActivity.this.getResources(),R.drawable.ic_mode_edit_black_24dp,ProfileActivity.this.getTheme()));

        nameTv.setText(new PrefManager(this).getName());
        typeTv.setText(new PrefManager(this).getType());

        delete.setOnClickListener(v -> {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Account delete")
                    .setMessage("Do you really want to delete your account? all your data will be deleted and there is no recovery option!")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.cancel();

                    }).setPositiveButton("Delete", (dialogInterface, i) -> {
                String stId=new PrefManager(this).getRegistrationToken();
                if(stId!="none"){
                    FirebaseFirestore.getInstance().collection("stations").document(stId).delete();
                }
                FirebaseFirestore.getInstance().collection("users").document(new PrefManager(this).getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        clearAppData();
                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                        finish();
                    }
                });

            }).show();
        });

        edit.setOnClickListener(v->{

  show();
        });
    }


    public void show() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Change name and email");
        builder.setMessage("Enter your new name and new email to use on Fuelio-garage");

        final EditText input = new EditText(ProfileActivity.this);


        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setSingleLine(false);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        input.setHint("New name");
        input.setMinLines(1);
       // input.setText(name);



        builder.setView(input);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String text = input.getText().toString();

            if(text.split(" ").length==1){

                Toast.makeText(ProfileActivity.this, "Enter both first name and last name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (text.equals("")) {
                Toast.makeText(ProfileActivity.this, "Empty names not allowed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Name and email will be changed soon", Toast.LENGTH_SHORT).show();

                new PrefManager(ProfileActivity.this).setName(text);

                nameTv.setText(text);

                DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(new PrefManager(ProfileActivity.this).getId());
                Map<String, Object> d = new HashMap<>();
                d.put("first_name", text.split(" ")[0]);
                d.put("last_name", text.split(" ")[1]);

                reference.set(d, SetOptions.merge());

            }

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
