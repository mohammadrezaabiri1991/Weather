package com.mohammadreza.weather.application;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;


public class MyApp extends Application {
    public final static String URL_FORMAT_FORECAST_DARK_SKY = "https://api.darksky.net/forecast/af561b9ba948fe89a087de963fd1137e/";
    public static String MAP_BOX_TOKEN = "pk.eyJ1IjoibW9oYW1tYWRyZXphYWJpcmkiLCJhIjoiY2syaXI4czIwMHh6MzNrcGN2YXJwMW4zYiJ9.O_zgIbOZDR-wzDt-DIBH_g";


    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }


}
