package com.fuelio.fuelio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import mehdi.sakout.fancybuttons.FancyButton;

public class IntroActivity extends AppCompatActivity {

    FancyButton start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        start = findViewById(R.id.start);

        start.setOnClickListener(v-> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        if (!new PrefManager(this).isFirstTimeLaunch()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


    }

}
