package com.fuelio.fuelio;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;


/**
 * Created by Regis on 2/13/2019.
 */

public class Application extends android.app.Application {

    public Application(){

    }
    static{
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
