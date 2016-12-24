package com.example.lenovo.AugGraffiti;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.Toast;
import static android.hardware.SensorManager.getAltitude;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.Manifest;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/*
This Activity loads the GoogleMapScreen and places a P-Tag on user's current location.
And also, monitors the change in User's location and updates the P-tag. It also fetches
the near tags if available from the Web_API and displays it as C-tag on the map screen.
It also implements Sign-out and Get-score functionalities
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationChangeListener, GoogleMap.OnMarkerClickListener {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public static final String EMAIL_ADDRESS = "userEmailId";
    String userEmailID = null;
    GoogleApiClient gac;
    private GoogleMap mMap;
    Button signOutbutton;
    LocationTracker gps;
    LatLng currentLocation;
    WebApiServer web;
    MarkerOptions currentLocMarker;
    double altitude=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("in maps", "on create");
        //Get the Email ID from MainActivity
        userEmailID = getIntent().getStringExtra(EMAIL_ADDRESS);

        //Initialize LocationService using current activity's context
        gps = new LocationTracker(MapsActivity.this);

        /*Initialize locationChangeListener to current class object
        such that whenever there is a change in the location,onLocationChange
        method which is implemented below is called
         */
        gps.locationChangeListener = this;


    }

    @Override
    protected void onStart() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gac = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        gac.connect();
        super.onStart();
    }


    //On Destroy, set the Map to null.
    @Override
    public void onDestroy() {
        mMap = null;
        super.onDestroy();
    }


    /*On Location Change, clear the current Markers on the map
    and call showMapCursor to update the P and C tags
    */
    @Override
    public void onLocationChange(Double lat, Double lng) {
        Log.d("Got LOCLAT ONCHNGED", String.valueOf(lat));
        Log.d("Got LOCLONG ONCHNGW", String.valueOf(lng));
        mMap.clear();
        showMapCursor();

    }

    /*
    When the Map is ready, set mMap and call showMapCursor for
    displaying P and C tags
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showMapCursor();


    }

    /*
    This method updates the P tag on the Map screen and finds near tags by calling
    the Web server and updates the C tags.
     */
    public void showMapCursor() {
        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            altitude= gps.getAltitude();

            Log.d("Got Loc lat in maps", String.valueOf(latitude));
            Log.d("Got LOC long in maps", String.valueOf(longitude));
            currentLocation = new LatLng(latitude, longitude);

            //Spoofing the current location to COOR HALL's coordinates
           // currentLocation=new LatLng(Double.parseDouble("33.4195"),Double.parseDouble("-111.939108"));

            Log.d("Add current loc", "curr");

            //Crop the Image width and Height
            Bitmap smallCurrentMarker = cropImage(75, 75, R.drawable.download);

            //Add the P tag on the map and zoom the cameraView , so that the Tag is visible
            currentLocMarker = new MarkerOptions().position(currentLocation).title("CurrentLoc").draggable(true).icon(BitmapDescriptorFactory.fromBitmap(smallCurrentMarker));
            mMap.addMarker(currentLocMarker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));


        } else {
            //Not able to get CurrentLocation
            return;
        }

        //  mMap.clear();

        //Make a Post request to the Web-server and pass the email ID and Current location as an input
        StringRequest myStringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.web_api) + "neartags.php",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String MyResponse) {
                        Log.d("Response from loclaot ", MyResponse);
                        if (!MyResponse.isEmpty()) {
                            //Crop the Image width and height
                            Bitmap smallCIconMarker = cropImage(75, 75, R.drawable.c_icon);
                            String[] arr = MyResponse.split(",");

                            /*
                            Get the latitude and Longitude values from the comma separated response
                            and add the C tags on the map and zoom the cameraView, so that the Tag is visible
                             */
                            for (int i = 0; i < arr.length; i += 3) {
                                String tag_id=arr[i];
                                LatLng location = new LatLng(Double.parseDouble(arr[i + 2]), Double.parseDouble(arr[i + 1]));
                                mMap.addMarker(new MarkerOptions().position(location).title(tag_id).draggable(true).icon(BitmapDescriptorFactory.fromBitmap(smallCIconMarker)));
                               //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "No tags around the current location", Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //viewText.setText("Response is: " + error);
                Log.e("Response", String.valueOf(error));
                Toast.makeText(getApplicationContext(), "Error getting a response from Server. Please try later", Toast.LENGTH_LONG).show();


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param_map = new HashMap<>();
                param_map.put("email", userEmailID);
                param_map.put("loc_long", String.valueOf(currentLocation.longitude));
                param_map.put("loc_lat", String.valueOf(currentLocation.latitude));
                Log.d("putting loccat", "done");
                return param_map;
            }
        };
        WebApiServer.getInstance(getApplicationContext()).addToQueue(myStringRequest);

        mMap.setOnMarkerClickListener(this);
    }

    /*
    Signout from the GoogleSignIn API and redirect to the Main Activity
     */
    public void onSignout(View view) {

        Log.d("Calling signout", "calling sout");
        Auth.GoogleSignInApi.signOut(gac).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Toast.makeText(getApplicationContext(), "LoggedOut", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }


                }
        );
    }

    /*
    Make a Post request to the  Web-server and pass EmailID as an input and display the score of
    the user in a Alert Box
     */
    public void getScore(View view) {
        StringRequest myStringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.web_api) + "getscore.php",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String MyResponse) {
                        Log.d("Response", MyResponse);
                        if (!MyResponse.contentEquals("-1")) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                            builder.setMessage("Your Current Score is : " + MyResponse)
                                    .setCancelable(true);
                            final AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error getting a response from Server. Please try later", Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response", String.valueOf(error));
                Toast.makeText(getApplicationContext(), "Error getting a response from Server. Please try later", Toast.LENGTH_LONG).show();


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param_map = new HashMap<>();
                param_map.put("email", userEmailID);
                Log.d("putting email", "done");
                return param_map;
            }
        };
        WebApiServer.getInstance(getApplicationContext()).addToQueue(myStringRequest);


    }

    /*
    Re-size the corresponding image to the given height and width
    and return it.
     */
    public Bitmap cropImage(int height, int width, int image) {
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(image);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallCurrentMarker = Bitmap.createScaledBitmap(b, width, height, false);
        return smallCurrentMarker;
    }
/*
Gets all the collected tags from the post request made to the server and displays it in a grid view.
 */
    public void getGallery(View view){
        StringRequest myStringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.web_api) + "getgallery.php",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String MyResponse) {
                        Log.d("server resp_for_gallery", MyResponse);
                        if(!MyResponse.isEmpty()){
                            String[]list= MyResponse.split(",");

                            Intent intent = new Intent(MapsActivity.this,GalleryView.class);
                            ArrayList<String> stuff = new ArrayList<String>();
                            for (int i = 0; i < list.length; i++) {
                                stuff.add(list[i]);
                            }

                            intent.putStringArrayListExtra("list_of_urls", stuff);
                            startActivity(intent);

                        }
                        else{
                            Log.d("server resp_for_gallery", MyResponse);
                            Toast.makeText(getApplicationContext(), "No Tags Collected by the User", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response", String.valueOf(error));
                Toast.makeText(getApplicationContext(), "Error getting a response from Server. Please try later", Toast.LENGTH_LONG).show();


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param_map = new HashMap<>();
                param_map.put("email", userEmailID);
                Log.d("putting gallery", "done");
                return param_map;
            }
        };
        WebApiServer.getInstance(getApplicationContext()).addToQueue(myStringRequest);



    }

    /* This method call provides the necessary intent to the Place Screen and Collect screen*/
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("MArker name",marker.getTitle());
        if(marker.getTitle().equals(currentLocMarker.getTitle())){
            Log.d("MArker name is Same",marker.getTitle());



                Intent intent = new Intent(MapsActivity.this, PlaceScreenActivity.class);
                intent.putExtra("latitude", String.valueOf(currentLocation.latitude));
                intent.putExtra("longitude", String.valueOf(currentLocation.longitude));
                intent.putExtra("email", userEmailID);
                startActivity(intent);
                Log.d("Intent has started",marker.getTitle());


        }
        else{
            Log.d("MArker name is Not Same",marker.getTitle());
            String tag_id=marker.getTitle();
            Intent intent = new Intent(MapsActivity.this,CollectScreenActivity.class);
            intent.putExtra("tagId",tag_id);
            intent.putExtra("altitude",String.valueOf(altitude));
            intent.putExtra("email", userEmailID);
            startActivity(intent);



        }
        return false;
    }

}
