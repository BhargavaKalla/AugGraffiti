package com.example.lenovo.AugGraffiti;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


/*
Location Tracker is a service which uses  Location manager
and Location listener to get the current Location of the user
and also tracks the location of the user.
 */
public class LocationTracker extends Service {
    private Context mContext;
    public LocationChangeListener locationChangeListener;


    // flag for GPS status
    public boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    double altitude;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 30; // 30 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 3; // 1000 * 60 * 3 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    @Override
    public void onCreate() {
        Log.d("herein", "onCreate: ");
    }


    public LocationTracker(Context context) {
        this.mContext = context;
        Log.d("here", "Constructor");

        getLocation();
    }

    public LocationTracker() {
    }

    //Location Listener is used to track the location of the user
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        //Whenever the user location changes, onLocationChanged method is called
        public void onLocationChanged(final Location location) {
            if (location != null) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude=location.getAltitude();
                //OnLocationChange method of LocationChangeListener is called
                if (locationChangeListener != null) {
                    locationChangeListener.onLocationChange(latitude, longitude);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void getLocation() {
        try {
            Log.d("here", "here");
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled'"
                Log.d("NO NETWORK OR GPS", "NO Network OR GPS");

            }
            //Get the current Location of the User
            else {


                this.canGetLocation = true;
                // if Network is Enabled, get lat/long using Network Carrier Services
                if (isNetworkEnabled) {
                    location = null;
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            altitude=location.getAltitude();
                            Log.d("Got LOCATION LATITFRmNW", String.valueOf(latitude));
                            Log.d("Got LOCATION LONGIFRmNW", String.valueOf(longitude));
                        }
                    }
                }
                // if GPS is Enabled, get lat/long using GPS Services
                if (isGPSEnabled) {
                    location = null;
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                altitude=location.getAltitude();
                                Log.d("Got LOCATION LATIT", String.valueOf(latitude));
                                Log.d("Got LOCATION LONGI", String.valueOf(longitude));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e("Exception", String.valueOf(e));
            e.printStackTrace();
        }

    }


    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }
    /**
     * Function to get altitude
     */
    public double getAltitude() {
        if (location != null) {
            altitude = location.getAltitude();
        }

        return latitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
