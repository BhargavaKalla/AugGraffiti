package com.example.lenovo.AugGraffiti;

import android.*;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;


/*
In this activity, GoogleSignIn implementation is provided. Once, the user signs in
successfully, LocationTracker service is started and MapActivity is called.
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private SignInButton signInBut;
    private static GoogleApiClient gac;
    private GoogleSignInOptions signInOpt;
    public static final int reqCode = 100;
    LocationTracker gps;
    Intent serviceIntent;
    private String emailAddress;
    //String emailAddress = null;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Configure SignIn to request User's Email Address and other Account info included in DEFAULT_SIGN_IN
        signInOpt = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        //Create a googleApiClient and add the Google_SIGN_IN_API
        gac = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOpt).build();
        Log.d("TAG", "here in main ");


        signInBut = (SignInButton) findViewById(R.id.signIn);

        //Render the GoogleSign button for acquiring SignIn scopes
        signInBut.setSize(SignInButton.SIZE_WIDE);
        signInBut.setScopes(signInOpt.getScopeArray());
        Log.d("TAG", "All set ");
        signInBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Check For location permissions for ACCESS_FINE_LOCATION.
                 If the permission is not set, call requestPermissions method which
                 prompts the user to allow access
                 */

                int hasACCESS_FINE_LOCATIONPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
                if (hasACCESS_FINE_LOCATIONPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
                int hasACCESS_COARSE_LOCATIONPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
                if (hasACCESS_COARSE_LOCATIONPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }

                //Call GoogleSignIn API and track the result in OnActivity result method
                Log.d("TAG", "here in onClick ");
                Intent googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(gac);
                startActivityForResult(googleSignInIntent, reqCode);


            }
        });
    }

    /* If the SignIn is successful register the emailID to the server.
    On successful response, start LocationTracker service
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "on Activity result ");
        Log.d("ResultCode", String.valueOf(resultCode));
        if (requestCode == reqCode) {
            Log.d("TAG", "reqcode is equal ");
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("here", String.valueOf(signInResult.isSuccess()));


            if (signInResult.isSuccess()) {
                GoogleSignInAccount account = signInResult.getSignInAccount();
                emailAddress = account.getEmail();

                //Make a Post request to the Web-server and pass email ID as an input
                StringRequest myStringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.web_api) + "login.php",
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String MyResponse) {
                                Log.d("Response", MyResponse);
                                if (MyResponse.contentEquals("0")) {
                                    Log.d("calling tracking", "calling tracking");
                                    startTrackingLocation();
                                } else {

                                    //If the response from the server is not '0', then there is an error.
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
                        param_map.put("email", emailAddress);//
                        Log.d("putting email", "done");
                        return param_map;
                    }
                };
                //Adding the StringRequest to the RequestQueue
                WebApiServer.getInstance(getApplicationContext()).addToQueue(myStringRequest);


            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Something went wrong. Please try again ", Toast.LENGTH_LONG).show();
    }

    /*This method checks if the GPS for the mobile is turned on and if not, it prompts the user to turn on
    the GPS and then Starts the LocationTracker
       */
    public void startTrackingLocation() {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else {
            Log.d("Location", "Starting location tracker");
            initiateLocationTracker();
        }
    }

    //Create an alert builder and navigate to location services in settings to turn on the GPS
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please turn on your gps and then click on this button, do you want to turn on your gps now?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //Initiate LocationTracker service
    public void initiateLocationTracker() {
        Log.d("called location tarcker", "called");
        gps = new LocationTracker(MainActivity.this);
        serviceIntent = new Intent(MainActivity.this, LocationTracker.class);
        startService(serviceIntent);
        callMapsActivity();

    }

    /*
    This method calls Map Activity and sends the Email address of the user
    in Intent, which is used for Web_API calls in Map activity
     */
    public void callMapsActivity() {

        Log.d("called maps", "called");
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.EMAIL_ADDRESS, emailAddress);//emailAddress
        startActivity(intent);
    }

    /*Prompt the user when he is trying to quit the application and Navigate to
    home screen when he clicks on yes.
     */
    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to exit the application? ")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

}
