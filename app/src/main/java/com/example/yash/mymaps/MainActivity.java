package com.example.yash.mymaps;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status> {

    public static final String TAG = "MainActivity";
    public static final String GEOFENCE_ID ="MyGeofenceId";
    public static final String GEOFENCE_ID2 ="MyGeofenceId2";
    Button bt;
    double lat,lon;
    GoogleApiClient mGoogleApiClient=null;
    LocationRequest mlocationrequest;
    Intent intent;
    public ArrayList<Geofence> mGeofenceList;
    public static final int MY_FINE_LOCATION_PERMISSION=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt=(Button)findViewById(R.id.btaccess);
        mGeofenceList = new ArrayList<Geofence>();

        populategeofence();

        buildGoogleApiClient();

        Log.d(TAG,"Start Geofencing Monitoring");

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(MainActivity.this,MapsActivity.class);
                startActivity(in);
            }
        });


    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"Connected to Google Api Client");
        try {
            mlocationrequest = LocationRequest.create();
            mlocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mlocationrequest.setInterval(10000);
            requestLocationUpdates();
        } catch (SecurityException e){
            Log.d(TAG,"Security Exception - "+ e.getMessage());
        }

    }

    public void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},MY_FINE_LOCATION_PERMISSION);
            }
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mlocationrequest, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        lon=location.getLongitude();
        Toast.makeText(this,"latitude:"+lat+" "+"Longitude:"+lon,Toast.LENGTH_SHORT).show();
        Log.d(TAG,"latitude:"+lat+" "+"Longitude:"+lon);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case MY_FINE_LOCATION_PERMISSION:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                }
                else{
                    Toast.makeText(getApplicationContext(),"The permissions are required!!",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"Suspended Connection to Google Api Client");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"Failed to Connect to Google Api Client - " + connectionResult.getErrorMessage());
    }

    public void showToast(String e){
        Toast.makeText(this,e,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"On Resume Called");
        super.onResume();
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(response != ConnectionResult.SUCCESS){
            Log.d(TAG,"Google Play Services Not available - Show User dialog box");
            GoogleApiAvailability.getInstance().getErrorDialog(this,response,1).show();
        } else {
            Log.d(TAG,"Google Play services is available - No action required");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            Toast.makeText(this,"Start",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        ArrayList<String> geofenceIds = new ArrayList<String>();
        geofenceIds.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,geofenceIds);

        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private PendingIntent getGeofencePendingIntent() {

        intent = new Intent(this, GeofenceService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        Toast.makeText(this, "Inisde getGeofencePendingIntent", Toast.LENGTH_SHORT).show();
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public void  populategeofence(){
        mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(GEOFENCE_ID2)
                        .setCircularRegion(
                                30,
                                -84,
                                100
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(
                        23.33,
                        10.12,
                        100
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
                Toast.makeText(this, "added to geofence list", Toast.LENGTH_SHORT).show();

    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    public void onAddGeofences(View view) {
        if(!mGoogleApiClient.isConnected()){
            Toast.makeText(this,"Location Services not connected",Toast.LENGTH_SHORT).show();
        } else {
            try {
                Toast.makeText(this, "On Add Geofences Called", Toast.LENGTH_SHORT).show();

                if (!mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "GoogleApiClient Is not connected");
                } else {
                    LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent())
                            .setResultCallback(this);
                }

            // Result processed in onResult().
            Toast.makeText(this,"Intent fired",Toast.LENGTH_SHORT).show();

            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                Toast.makeText(this, "" + securityException, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Successfully added Geofence");
        } else {
            Log.d(TAG, "Failed To Add Geofence" + status.getStatus());

        }
    }
}
