package com.fuelio.fuelio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AdminActivity extends AppCompatActivity {

    CardView users,providers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        users=findViewById(R.id.users);
        providers=findViewById(R.id.providers);

        users.setOnClickListener(v->{
            startActivity(new Intent(this,AllUsersActivity.class));
        });

        providers.setOnClickListener(v->{
            startActivity(new Intent(this,AllProvidersActivity.class));
        });

        new PrefManager(getApplicationContext()).setType("admin");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(AdminActivity.this, ProfileActivity.class));

            return true;
        }else{

            new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are u sure you want to logout of your account?")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    }).setPositiveButton("Logout", (dialogInterface, i) -> {
                clearAppData();
                new PrefManager(AdminActivity.this).logout();
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();


            }).show();
        }


        return super.onOptionsItemSelected(item);
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
