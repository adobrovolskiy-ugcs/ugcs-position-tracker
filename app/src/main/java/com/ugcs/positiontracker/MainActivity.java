package com.ugcs.positiontracker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    protected static final int MY_PERMISSION_REQUEST_CODE = 0x1;

    protected static final int MY_CHECK_SETTINGS_REQUEST_CODE = 0x199;

    private FusedLocationProviderClient mFusedLocationClient = null;
    private LocationRequest mLocationRequest = null;
    private LocationCallback mLocationCallback = null;

    private int mLocationUpdatesCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check all necessary permissions and start location updates
        checkPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop receiving location updates
        stopLocationUpdates();
    }

    // Checks permissions required for the application
    private void checkPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CODE);
        } else {
            // permission has been granted, continue as usual
            checkLocationSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission granted!", Toast.LENGTH_LONG).show();
                    // permission was granted
                    checkLocationSettings();
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied.", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    // Checks location settings
    private void checkLocationSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(MainActivity.this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied.
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                MY_CHECK_SETTINGS_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MY_CHECK_SETTINGS_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        Toast.makeText(MainActivity.this, "Location enabled by user!", Toast.LENGTH_LONG).show();
                        // Start getting coordinates
                        startLocationUpdates();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(MainActivity.this, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }


    private LocationRequest createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void startLocationUpdates() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get last known location of the device
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        dumpLocation(location);
                    }
                });

        mLocationUpdatesCounter = 0;

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    dumpLocation( location);
                    setViewText( R.id.lbLocationUpdatesCounter, ++mLocationUpdatesCounter);

                    if( mLocationUpdatesCounter == Integer.MAX_VALUE)
                        mLocationUpdatesCounter = 0;
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);

    }

    private void stopLocationUpdates() {
        if( mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationUpdatesCounter = 0;
        }
    }

    private void dumpLocation(Location location) {
        if (location != null) {

            setViewText(R.id.lbLatitude, Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
            setViewText(R.id.lbLongitude, Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
            setViewText(R.id.lbAccuracy, location.getAccuracy());
            setViewText(R.id.lbNumberOfSatellites, getNumberOfSatellites(location));
            setViewText(R.id.lbAltitude, location.getAltitude());
            setViewText(R.id.lbGroundSpeed, location.getSpeed());
            setViewText(R.id.lbBearing, location.getBearing());
            setViewText(R.id.lbLocationProvider, location.getProvider());
        }
    }

    private void setViewText(int viewId, @NonNull String value) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(value);
        }
    }

    private void setViewText(int viewId, double value) {
        setViewText(viewId, String.valueOf(value));
    }

    private void setViewText(int viewId, float value) {
        setViewText(viewId, String.valueOf(value));
    }

    private void setViewText(int viewId, int value) {
        setViewText(viewId, String.valueOf(value));
    }

    private int getNumberOfSatellites(Location location) {
        Bundle bundle = location.getExtras();
        if (bundle != null)
            return bundle.getInt("satellites");
        else
            return 0;
    }


    /**
     * Called when the user taps the Send button
     */
    /*
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void openLocationMonitor(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
    */

}
