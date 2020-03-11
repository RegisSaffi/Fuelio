package com.fuelio.fuelio;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class Splash extends AppCompatActivity {

    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        setTitle("");

        type=new PrefManager(this).getType();
       // Toast.makeText(this, type, Toast.LENGTH_SHORT).show();

        if (!new PrefManager(this).isFirstTimeLaunch()) {
            new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long l) {

                }
                @Override
                public void onFinish() {

                    if(!type.equals("user")){
                        startActivity(new Intent(getApplicationContext(), StationActivity.class));
                        finish();
                    }else {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }
            }.start();
        }else{

            startActivity(new Intent(getApplicationContext(), IntroActivity.class));
            finish();
        }

    }

}
