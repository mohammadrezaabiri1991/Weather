package com.mohammadreza.weather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashSet;
import java.util.Set;


public class NetworkStateReceiver extends BroadcastReceiver {

    protected Set<NetworkStateReceiverListener> netListeners;

    protected Boolean connected;

    public NetworkStateReceiver() {
        netListeners = new HashSet<>();
        connected = null;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null)
            return;


        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = null;
        if (manager != null) {
            ni = manager.getActiveNetworkInfo();
        }

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            connected = false;
        }

        notifyStateToAll();


    }

    private void notifyStateToAll() {
        for (NetworkStateReceiverListener listener : netListeners)
            notifyState(listener);
    }


    private void notifyState(NetworkStateReceiverListener listener) {
        if (connected == null || listener == null)
            return;
        if (connected)
            listener.networkAvailable();
    }

    public void addListener(NetworkStateReceiverListener l) {
        netListeners.add(l);
        notifyState(l);
    }

    public interface NetworkStateReceiverListener {
        void networkAvailable();
    }

    public interface GpsStateReceiverListener {
        void gpsUnavailable();
    }

}