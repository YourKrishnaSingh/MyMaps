package com.example.yash.mymaps;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by krishnasingh on 05/05/17.
 */

public class GeofenceService extends IntentService{

    public static final String TAG = "GeofenceService";
    public static final String GEOFENCE_ID ="MyGeofenceId";
    public static final String GEOFENCE_ID2 ="MyGeofenceId2";

    Location location;
    Double lat,lon;
    Context context;

    BluetoothAdapter blueADP;
    AudioManager audioManager;
    WifiManager onwifi;

    Handler mHandler;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceService(String name) {
        super(name);
    }
    public GeofenceService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG," On Create");
        context = getApplicationContext();

        audioManager = (AudioManager) getSystemService(context.AUDIO_SERVICE);
        onwifi = (WifiManager) this.getSystemService(context.WIFI_SERVICE);
        blueADP = BluetoothAdapter.getDefaultAdapter();

        //for Toast in on HandleIntent
        mHandler = new Handler();

        Toast.makeText(context,"On Create",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if(event.hasError()){
            //ToDo:Handle Error
            Log.i(TAG,"In Geofence Error");
        } else {
            int transition = event.getGeofenceTransition();
            List<Geofence> geofences = event.getTriggeringGeofences();
            location = event.getTriggeringLocation();
            lat = location.getLatitude();
            lon = location.getLongitude();

            Geofence geofence = geofences.get(0);
            String requestId = geofence.getRequestId();

            if(transition==Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.i(TAG,"Entering Geofence :-"+requestId);

                if(requestId.equals(GEOFENCE_ID)){
                    Log.i(TAG,"Geofence Id");

                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,255);

                    onwifi.setWifiEnabled(true);

                    if(blueADP!=null) {
                        blueADP.enable();
                    }

                    //Settings.Global.putString(getContentResolver(),"AIRPLANE_MODE_ON","1");

                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);


                } else if(requestId.equals(GEOFENCE_ID2)){
                    Log.i(TAG,"Geofence Id 2");
                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,100);
                    onwifi.setWifiEnabled(false);
                    if(blueADP!=null) {
                        blueADP.enable();
                    }
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                } else {
                    Log.i(TAG,"Geofence Unknown");
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GeofenceService.this, "Entering Geofence", Toast.LENGTH_SHORT).show();
                    }
                });

            } else if(transition==Geofence.GEOFENCE_TRANSITION_EXIT){
                Log.i(TAG,"Exiting Geofence :-"+requestId);
                android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,125);
                onwifi.setWifiEnabled(false);
                if(blueADP!=null) {
                    blueADP.disable();
                }
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

            }
        }

    }
}
