package com.mohammadreza.weather.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mohammadreza.weather.constant.WeatherConstant;

public class Utils {
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    public static void showAlertDialog(final AppCompatActivity appCompatActivity, final String message, Button btnGetLocation) {
        new AlertDialog.Builder(appCompatActivity)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (btnGetLocation != null) {
                        btnGetLocation.setClickable(true);
                    }
                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static ProgressDialog showProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(WeatherConstant.PLEASE_WAIT);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }
}
