package com.mohammadreza.weather.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceSelectionListener;
import com.mohammadreza.weather.MyLocation;
import com.mohammadreza.weather.R;
import com.mohammadreza.weather.application.MyApp;
import com.mohammadreza.weather.constant.WeatherConstant;
import com.mohammadreza.weather.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;


public class AddCityDialog extends DialogFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static Dialog dialog;
    public CircularProgressBar prgGetCurrentLocation;
    private static final String TAG = "M_TAG";

    public static ProgressDialog dialogLoadWeather;
    public Button btnGetCurrentLocation;
    private String isEnterFirstTime = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialog);
        if (getActivity() != null) {
            ((WeatherMainActivity) (getActivity())).setStatusColor((AppCompatActivity) getActivity(), R.color.colorPrimary);
            isEnterFirstTime = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(WeatherConstant.LAT_LNG, "");
        }
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getActivity() != null) {
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            }
        }


        View view = inflater.inflate(R.layout.weather_add_city_dialog, container, true);
        dialog = getDialog();
        prgGetCurrentLocation = view.findViewById(R.id.dialog_get_current_progress);
        btnGetCurrentLocation = view.findViewById(R.id.btnGetCurrentLocation);

        view.findViewById(R.id.btnGetCurrentLocation).setOnClickListener(v -> {
            v.setClickable(false);
            checkPermissionAndGetCurrentLocation(v);
        });


        PlaceAutocompleteFragment autocompleteFragment = PlaceAutocompleteFragment.newInstance(MyApp.MAP_BOX_TOKEN);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, autocompleteFragment, TAG);
        transaction.commit();
        PlaceAutocomplete.clearRecentHistory(getActivity());


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(CarmenFeature carmenFeature) {
                if (carmenFeature != null && carmenFeature.placeName() != null && carmenFeature.center() != null && getActivity() != null) {
                    int k = carmenFeature.placeName().indexOf(" ", carmenFeature.placeName().indexOf(" ") + 1);
                    String placeName;
                    if (k > 0) {
                        placeName = carmenFeature.placeName().substring(0, k);
                    } else {
                        placeName = carmenFeature.placeName();
                    }

                    String result = placeName.replaceAll("[-+.^:,]", "");
                    String latLng = carmenFeature.center().latitude() + "," + carmenFeature.center().longitude();
                    ((WeatherMainActivity) getActivity()).getCityWeatherData(latLng, result, (AppCompatActivity) getActivity());
                    dialogLoadWeather = Utils.showProgressDialog(getActivity());
                }
            }

            @Override
            public void onCancel() {
                if (getActivity() != null) {
                    if (!isEnterFirstTime.isEmpty()) {
                        dismiss();
                    } else {
                        getActivity().finish();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        if (getActivity() != null) {
            ((WeatherMainActivity) (getActivity())).getStatusColor();
        }
        super.onDestroy();
    }

    public void checkGpsEnabled() {
        if (getActivity() != null) {
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1);
            locationRequest.setFastestInterval(1);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(mGoogleApiClient, builder.build());


            result.setResultCallback(result1 -> {

                final Status status = result1.getStatus();
                final LocationSettingsStates state = result1.getLocationSettingsStates();

//            if (isOnline(Objects.requireNonNull(getContext()))) {
                if (state.isGpsUsable()) {
                    getLocationFromMyLocationClass(getContext());
                } else {
                    try {
                        status.startResolutionForResult(getActivity(), 1000);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                }
//            } else {
//                Toast.makeText(getContext(), getContext().getString(R.string.no_access_to_net), Toast.LENGTH_SHORT).show();
//                btnGetCurrentLocation.setClickable(true);
//            }

            });
        }

    }

    public void getLocationFromMyLocationClass(final Context context) {

        prgGetCurrentLocation.setVisibility(View.VISIBLE);

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void gotLocation(final Location location) {

                if (location != null) {
                    new Thread(() -> getLocationAddress(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), context)).start();

                } else {
                    handleGetLocationError();
                }
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(context, locationResult);


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getLocationAddress(final String lat, String lng, final Context context) {

        final String latLng = lat + "," + lng;
        final String lngLat = lng + "," + lat;

//        String strUrl = "https://geocode.xyz/" + latLng + "?json=1";
        String strUrlMapBox = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + lngLat + ".json?access_token=pk.eyJ1IjoibW9oYW1tYWRyZXphYWJpcmkiLCJhIjoiY2syaXJhM2htMGxvMTNubzBpdTNrdGtmMSJ9.mNgE1ohUhn4ik83J2iBwFA";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, strUrlMapBox, null, response -> {
            try {
//                    String locationName = response.getString("city");
                JSONArray features = response.getJSONArray("features");
//                    String locationName = features.getJSONObject(0).getString("text");
                String locationName = features.getJSONObject(0).getString("place_name");
                String[] separated = locationName.split(",");
                String cityName = separated[1];
                if (!cityName.isEmpty() && getContext() != null) {
//                    if (isOnline(Objects.requireNonNull(getContext()))) {
                    ((WeatherMainActivity) getContext()).getCityWeatherData(latLng, cityName, (AppCompatActivity) getActivity());
//                    } else {
//                        Toast.makeText(context, getString(R.string.yourDeviceIsOffline), Toast.LENGTH_SHORT).show();
//                    }
                }
            } catch (JSONException e) {
                handleGetLocationError();
            }
        }, error -> {

            if (getActivity() != null) {
                ((WeatherMainActivity) (getActivity())).handleVolleyRequestError();
            }


        });

        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

    private void handleGetLocationError() {
        if (getActivity() != null) {
            (getActivity()).runOnUiThread(() -> {
                if (prgGetCurrentLocation != null) {
                    prgGetCurrentLocation.setVisibility(View.INVISIBLE);
                }
                Utils.showAlertDialog((AppCompatActivity) getActivity(), getResources().getString(R.string.cannot_get_location), btnGetCurrentLocation);
            });
        }
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (getActivity() != null) {
                    if (!isEnterFirstTime.isEmpty()) {
                        dismiss();
                    } else {
                        getActivity().finish();
                    }
                }

            }
            return false;
        });
        return dialog;
    }


    private void checkPermissionAndGetCurrentLocation(View v) {
        if (getActivity() != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ActivityCompat.checkSelfPermission((getActivity()),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

            } else {
                checkGpsEnabled();
                gpsCheckForApiUnder21(v);
            }
        }
    }

    public void gpsCheckForApiUnder21(View v) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (getActivity() != null) {
                final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps(v);
                } else if (manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    getLocationFromMyLocationClass(getContext());
                }
            }
        }
    }

    private void buildAlertMessageNoGps(View v) {
        if (getActivity() != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.enable_gps_text)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, (dialog, id) -> {
                        v.setClickable(true);
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        v.setClickable(true);
                        dialog.cancel();
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            assert dialog.getWindow() != null;
            dialog.getWindow().setLayout(width, height);
        }
    }
}