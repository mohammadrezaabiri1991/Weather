package com.mohammadreza.weather.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mohammadreza.weather.R;
import com.mohammadreza.weather.constant.WeatherConstant;
import com.mohammadreza.weather.receiver.NetworkStateReceiver;
import com.mohammadreza.weather.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;

import static com.mohammadreza.weather.Initialization.calculateWeatherStateForecast;
import static com.mohammadreza.weather.Initialization.getCurrentDayData;
import static com.mohammadreza.weather.Initialization.loadSharedPreferencesCurrentDay;
import static com.mohammadreza.weather.Initialization.loadSharedPreferencesForeCast;
import static com.mohammadreza.weather.application.MyApp.URL_FORMAT_FORECAST_DARK_SKY;
import static com.mohammadreza.weather.ui.AddCityDialog.dialog;
import static com.mohammadreza.weather.ui.AddCityDialog.dialogLoadWeather;


public class WeatherMainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    private TextView txtWeatherState;
    private TextView txtCityName;
    private TextView txtCityTemp;
    private ImageView imgWeatherImage;
    private TextView txtNextDay;
    private TextView txtNextDayTemp;
    private TextView txtNextDayIcon;
    private TextView txtSecondDay;
    private TextView txtSecondDayTemp;
    private TextView txtSecondDayIcon;
    private TextView txtThirdDay;
    private TextView txtThirdDayTemp;
    private TextView txtThirdDayIcon;
    private ImageView imgWindSpeed;
    private TextView txtWindSpeed;
    private TextView txtPrecipitationChance;
    private TextView txtHumidity;

    private ImageView imgHeaderLinearDetails;

    private LinearLayout linearDetails;
    private LinearLayout linear_loading;

    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isOfflineMode;
    private String latLng = "";
    private String cityName = "";
    private int arrowColor;
    private int numberOfClick;
    private boolean autoUpdate;

    private NetworkStateReceiver networkStateReceiver;

    private AddCityDialog addCityDialog;


    @Override
    protected void onResume() {
        super.onResume();
        getStatusColor();

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity_main);


        if (!Utils.isOnline(this)) {
            isOfflineMode = true;
        }

        ImageView imgAddCity = findViewById(R.id.imgAddCity);
        imgAddCity.setOnClickListener(v -> {
            if (dialog != null && dialog.isShowing() && !swipeRefreshLayout.isRefreshing()) {
                return;
            }
            addCityDialog = new AddCityDialog();
            addCityDialog.show(getSupportFragmentManager(), getString(R.string.my_dialog_fragment));


        });


        txtWeatherState = findViewById(R.id.txtWeatherState);
        txtCityName = findViewById(R.id.txtCityName);
        imgWeatherImage = findViewById(R.id.imgWeatherIcon);
        txtCityTemp = findViewById(R.id.txtTemp);

        txtNextDay = findViewById(R.id.txtNextDay);
        txtNextDayTemp = findViewById(R.id.txtNextDayTemp);
        txtNextDayIcon = findViewById(R.id.txtNextDayIcon);

        txtSecondDay = findViewById(R.id.txtSecondDay);
        txtSecondDayTemp = findViewById(R.id.txtSecondDayTemp);
        txtSecondDayIcon = findViewById(R.id.txtSecondDayIcon);

        txtThirdDay = findViewById(R.id.txtThirdDay);
        txtThirdDayTemp = findViewById(R.id.txtThirdDayTemp);
        txtThirdDayIcon = findViewById(R.id.txtThirdDayIcon);

        imgWindSpeed = findViewById(R.id.img_wind_speed);
        txtWindSpeed = findViewById(R.id.txt_wind_speed);

        txtPrecipitationChance = findViewById(R.id.txt_precipitation_chance);

        txtHumidity = findViewById(R.id.txt_humidity);

        swipeRefreshLayout = findViewById(R.id.swipe_parent);

        imgHeaderLinearDetails = findViewById(R.id.img_hide_linear_details);
//        ImageView imgShowLinearDetails = findViewById(R.id.img_show_linear_details);

        linearDetails = findViewById(R.id.linear_details);
        linear_loading = findViewById(R.id.linear_loading);


        setWeatherData();

        swipeRefreshLayout.setOnRefreshListener(() -> {

            getCityWeatherData(latLng, cityName, WeatherMainActivity.this);

            if (Utils.isOnline(WeatherMainActivity.this)) {
                if (!latLng.isEmpty() && !cityName.isEmpty()) {
                    getCityWeatherData(latLng, cityName, WeatherMainActivity.this);
                }

            }
        });

        imgHeaderLinearDetails.setOnClickListener(this::actionImgLinearDetails);
        linearDetails.setOnClickListener(this::actionImgLinearDetails);

//        imgHeaderLinearDetails.setOnClickListener(this::actionImgLinearDetails);


    }

    private void actionImgLinearDetails(final View v) {
//        if (v.getTag().equals(getString(R.string.img_hide_linear_details))) {


        if (linearDetails.getVisibility() == View.VISIBLE) {
//            Animation arrowRotateToDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.weather_arrow_rotate_to_down);
            Animation animHideLinearDetails = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.weather_linear_details_hide);
//            imgHeaderLinearDetails.setAnimation(arrowRotateToDown);

            linearDetails.setAnimation(animHideLinearDetails);
            linearDetails.setVisibility(View.GONE);

            setAnimToArrowImage(0, 180);

        } else {
//            Animation arrowRotateToUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.weather_arrow_rotate_to_up);
            Animation animShowLinearDetails = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.weather_linear_details_show);

//            imgHeaderLinearDetails.setAnimation(arrowRotateToUp);
            linearDetails.setAnimation(animShowLinearDetails);
            linearDetails.setVisibility(View.VISIBLE);

            setAnimToArrowImage(180, 0);


        }
//        } else if (v.getTag().equals(getString(R.string.img_show_linear_details))) {
//            Animation animShowLinearDetails = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.weather_linear_details_show);
//            Animation arrowRotateToUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.weather_arrow_rotate_to_up);
//
//            if (linearDetails.getVisibility() != View.VISIBLE) {
//                linearDetails.setAnimation(animShowLinearDetails);
//                imgHeaderLinearDetails.setAnimation(arrowRotateToUp);
//                linearDetails.setVisibility(View.VISIBLE);
//
//            }
//        }
    }

    private void setAnimToArrowImage(int from, int to) {
        RotateAnimation rotate = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(400);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new LinearInterpolator());
        imgHeaderLinearDetails.startAnimation(rotate);

    }

    private void setWeatherData() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String latLngPreference = preferences.getString(WeatherConstant.LAT_LNG, "");
        String cityNamePreference = preferences.getString(WeatherConstant.CITY_NAME, "");

        if (!latLngPreference.isEmpty() && !cityNamePreference.isEmpty()) {
            linear_loading.setVisibility(View.GONE);
            latLng = latLngPreference;
            cityName = cityNamePreference;
            ArrayList<String> getCurrentDayResult = loadSharedPreferencesCurrentDay(this);


            txtCityName.setText(getCurrentDayResult.get(1));
            txtWeatherState.setText(getCurrentDayResult.get(2));
            txtCityTemp.setText(getCurrentDayResult.get(3));
            imgWeatherImage.setImageResource(getImageId(this, getCurrentDayResult.get(4)));

            txtWindSpeed.setText(String.valueOf(getCurrentDayResult.get(5)));
            imgWindSpeed.setImageDrawable(getImageByName(this, getCurrentDayResult.get(6)));
            txtPrecipitationChance.setText(getCurrentDayResult.get(7));
            txtHumidity.setText(String.valueOf(getCurrentDayResult.get(8)));


//---------------------------------------------------------------------------------------------------------------------------------------------
            ArrayList<String> getNextDayResult = loadSharedPreferencesForeCast(this, 1);
            txtNextDay.setText(getNextDayResult.get(0));
            txtNextDayTemp.setText(getNextDayResult.get(1));
            txtNextDayIcon.setText(getNextDayResult.get(2));

            ArrayList<String> getSecondDayResult = loadSharedPreferencesForeCast(this, 2);
            txtSecondDay.setText(getSecondDayResult.get(0));
            txtSecondDayTemp.setText(getSecondDayResult.get(1));
            txtSecondDayIcon.setText(getSecondDayResult.get(2));


            ArrayList<String> getThirdDayResult = loadSharedPreferencesForeCast(this, 3);
            txtThirdDay.setText(getThirdDayResult.get(0));
            txtThirdDayTemp.setText(getThirdDayResult.get(1));
            txtThirdDayIcon.setText(getThirdDayResult.get(2));


            if (Utils.isOnline(this)) {
                autoUpdate = true;
                getCityWeatherData(latLngPreference, cityNamePreference, WeatherMainActivity.this);
            }
        } else {
            addCityDialog = new AddCityDialog();
            addCityDialog.show(getSupportFragmentManager(), getString(R.string.my_dialog_fragment));
        }

    }

    public static Drawable getImageByName(Context context, String name) {
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(name, WeatherConstant.STR_DRAWABLE,
                context.getPackageName());
        return resources.getDrawable(resourceId);
    }

    private int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier(WeatherConstant.STR_MIPMAP + imageName, null, context.getPackageName());
    }

    public void getCityWeatherData(final String latLng, final String cityName,
                                   final AppCompatActivity appCompatActivity) {

        new Thread(() -> {

            WeatherMainActivity.this.latLng = latLng;
            WeatherMainActivity.this.cityName = cityName;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, URL_FORMAT_FORECAST_DARK_SKY + latLng, null, response -> {

                runOnUiThread(() -> {
                    try {
                        getCurrentDayData(response, appCompatActivity, latLng, cityName, txtCityName, txtWeatherState, txtCityTemp, imgWeatherImage, imgWindSpeed, txtWindSpeed, txtPrecipitationChance, txtHumidity, linear_loading);
                        calculateWeatherStateForecast(response, txtNextDay, txtNextDayTemp, txtNextDayIcon, 1, appCompatActivity);
                        calculateWeatherStateForecast(response, txtSecondDay, txtSecondDayTemp, txtSecondDayIcon, 2, appCompatActivity);
                        calculateWeatherStateForecast(response, txtThirdDay, txtThirdDayTemp, txtThirdDayIcon, 3, appCompatActivity);

                        swipeRefreshLayout.setRefreshing(false);
                    } catch (JSONException e) {
                        swipeRefreshLayout.setRefreshing(false);

                        if (addCityDialog != null && dialogLoadWeather != null) {
                            dialogLoadWeather.dismiss();
                        }
                        Utils.showAlertDialog(WeatherMainActivity.this, getString(R.string.failed_get_data), null);
                    }


                });

                if (!autoUpdate) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                } else {
                    autoUpdate = false;
                }


            }, error -> runOnUiThread(this::handleVolleyRequestError));


            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            Volley.newRequestQueue(WeatherMainActivity.this).add(jsonObjectRequest);

        }).

                start();

    }

    public void handleVolleyRequestError() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (addCityDialog != null && addCityDialog.prgGetCurrentLocation != null) {
            addCityDialog.prgGetCurrentLocation.setVisibility(View.INVISIBLE);
        }
        if (addCityDialog != null && dialogLoadWeather != null) {
            dialogLoadWeather.dismiss();
        }


        if (addCityDialog != null && addCityDialog.btnGetCurrentLocation != null) {
            Utils.showAlertDialog(WeatherMainActivity.this, getResources().getString(R.string.failed_get_data), addCityDialog.btnGetCurrentLocation);
        } else {
            Utils.showAlertDialog(WeatherMainActivity.this, getResources().getString(R.string.failed_get_data), null);
        }

    }


    @Override
    public void networkAvailable() {
        if (isOfflineMode && dialog == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String latLngPreference = preferences.getString(WeatherConstant.LAT_LNG, "");
            String cityNamePreference = preferences.getString(WeatherConstant.CITY_NAME, "");

            if (!latLngPreference.isEmpty() && !cityNamePreference.isEmpty()) {
                autoUpdate = true;
                getCityWeatherData(latLngPreference, cityNamePreference, WeatherMainActivity.this);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                addCityDialog = (AddCityDialog) getSupportFragmentManager().findFragmentByTag(getString(R.string.my_dialog_fragment));
                if (addCityDialog != null) {
                    addCityDialog.getLocationFromMyLocationClass(this);
                }
            } else {
                setClickableToBtnCurrentLocationDialog();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && permissions[0].contains(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addCityDialog = (AddCityDialog) getSupportFragmentManager().findFragmentByTag(getString(R.string.my_dialog_fragment));
            if (addCityDialog != null) {
                addCityDialog.checkGpsEnabled();
            }
        } else {
            setClickableToBtnCurrentLocationDialog();
        }

    }

    @Override
    public void onBackPressed() {
        if (addCityDialog != null && addCityDialog.getDialog() != null && addCityDialog.getDialog().isShowing()) {
            addCityDialog.getDialog().dismiss();
        } else {
            numberOfClick += 1;
            if (numberOfClick == 1) {
                Toast.makeText(this, R.string.exit_app_message, Toast.LENGTH_SHORT).show();
            } else if ((numberOfClick > 1)) {
                super.onBackPressed();
            }
            new Handler().postDelayed(() -> numberOfClick = 0, 2000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    private void setClickableToBtnCurrentLocationDialog() {
        if (addCityDialog != null && addCityDialog.btnGetCurrentLocation != null) {
            addCityDialog.btnGetCurrentLocation.setClickable(true);
        }
    }

    public void getStatusColor() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        arrowColor = preferences.getInt(WeatherConstant.STATUS_BAR_COLOR, R.color.colorPrimary);
        setStatusColor(this, arrowColor);
        setGradientToLinearDetails(linearDetails, arrowColor);
//        DrawableCompat.setTint(imgHeaderLinearDetails.getDrawable(), ContextCompat.getColor(this, arrowColor));

    }

    public void setStatusColor(AppCompatActivity appCompatActivity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appCompatActivity.getWindow().setStatusBarColor(getResources().getColor(color));
            appCompatActivity.getWindow().setStatusBarColor(getResources().getColor(color));
        }
    }

    private void setGradientToLinearDetails(LinearLayout linearDetails, int color) {
        int startEndColor = Color.parseColor("#aaffffff");
        GradientDrawable mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{startEndColor, getResources().getColor(color), startEndColor});


        if (linearDetails != null) {
            linearDetails.setBackgroundDrawable(mDrawable);
        }
    }


}