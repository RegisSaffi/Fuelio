package com.fuelio.fuelio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

public class AdminActivity extends AppCompatActivity {

    CardView users,providers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        users=findViewById(R.id.users);
        providers=findViewById(R.id.providers);

        users.setOnClickListener(v->{
            startActivity(new Intent(this,AllUsersActivity.class));
        });

        providers.setOnClickListener(v->{
            startActivity(new Intent(this,AllProvidersActivity.class));
        });
    }
}
