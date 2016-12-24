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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlaceScreenActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "PlaceTagActivity";
    private TextureView textureView;
    private String cameraId;
    private String imageTagString;
    private String currentLatitude;
    private String currentLongitude;
    public String emailAddress;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler myBackgroundHandler;
    private HandlerThread myBackgroundThread;
    private SensorManager mySensorManager = null;
    private Sensor myGravity;
    private Sensor myAccelerometer;
    private Sensor myMagnetometer;
    private Sensor myPressureSensor;
    boolean havePressureSensor = false;
    boolean haveGravitySensor = false;
    boolean haveAccelerometerSensor = false;
    boolean haveMagnetometerSensor = false;
    private int myAzimuth = 0; // in degrees
    private float myPressure;
    private int myAltitude;
    CanvasView myCanvasView;



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_screen);

        //getting the current latitude and longitude from the mapscreen activity
        currentLatitude=getIntent().getStringExtra("latitude");
        currentLongitude=getIntent().getStringExtra("longitude");

        //getting the email address from the mapscreen activity
        emailAddress=getIntent().getStringExtra("email");

        // this is used to  set the view as texture view
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;

        // this calls the surface texture listener of the texture view and creates the view from back facing camera
        textureView.setSurfaceTextureListener(myTextureListener);

        // this is used to  set the view as a canvas surface enabling to draw
        myCanvasView = (CanvasView)findViewById(R.id.draw);

        // this call is used to access the sensors of the device
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // this call is used is used to access the TYPE_GRAVITY sensor and registers the sensor event listener
        this.myGravity = this.mySensorManager.getDefaultSensor( Sensor.TYPE_GRAVITY );
        this.haveGravitySensor = this.mySensorManager.registerListener( mSensorEventListener, this.myGravity, SensorManager.SENSOR_DELAY_GAME );

        // this call is used is used to access the TYPE_ACCELEROMETER sensor and registers the sensor event listener
        this.myAccelerometer = this.mySensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        this.haveAccelerometerSensor = this.mySensorManager.registerListener( mSensorEventListener, this.myAccelerometer, SensorManager.SENSOR_DELAY_GAME );

        // this call is used is used to access the TYPE_MAGNETIC_FIELD sensor and registers the sensor event listener
        this.myMagnetometer = this.mySensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
        this.haveMagnetometerSensor = this.mySensorManager.registerListener( mSensorEventListener, this.myMagnetometer, SensorManager.SENSOR_DELAY_GAME );

        // this call is used is used to access the TYPE_PRESSURE sensor and registers the sensor event listener
        this.myPressureSensor = this.mySensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        this.havePressureSensor = this.mySensorManager.registerListener(mSensorEventListener,this.myPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // if there is a gravity sensor we do not need the accelerometer
        if( this.haveGravitySensor ) {
            this.mySensorManager.unregisterListener(this.mSensorEventListener, this.myAccelerometer);
        }


        /* Clicking the commit button below listener is called that takes the combined image of the tag
           along with camera view in the back
        */

        Button button = (Button) findViewById(R.id.Commitbutton);
        button.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {


                Bitmap save = myCanvasView.getBitmap();
                if (save == null) {
                    System.out.println("NULL bitmap save\n");
                }
                try {

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    save.compress(Bitmap.CompressFormat.JPEG, 70, stream);

                    byte[] byteFormat = stream.toByteArray();
                    String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
                    imageTagString = imgString; // storing the 64 base encoded placed tag image in jpeg format
                    Log.d("TAG", imgString);
                    sendPOSTRequest();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    // this listener enables the view of back facing camera and closes the camera once the surface is destroyed
    TextureView.SurfaceTextureListener myTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    // this method is called to open the back facing camera
    public void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Log.e(TAG, "BP ");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BP1 ");
                ActivityCompat.requestPermissions(PlaceScreenActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                Log.e(TAG, "AP ");
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            Log.e(TAG, "Unable to fetch image: ");
            e.printStackTrace();
        }
        Log.d(TAG, "open Camera");
    }



    // this callback method starts the preview of the back facing camera
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is opened
            Log.d(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };


    // this method is called to start the camera preview
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    if (null == cameraDevice) {
                        return;
                    }

                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(PlaceScreenActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    // this method updates the camera preview and helps in continuous streaming of the back facing camera
    protected void updatePreview() {

        if (null == cameraDevice) {
            Log.e(TAG, "update Preview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, myBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    protected void onResume() {
        super.onResume();

        // This method starts a background thread and continuously streams the backfacing camera view

        Log.d(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(myTextureListener);
        }
    }

    @Override
    protected void onPause() {

        // This method stops the camera view and unregisters the sensors when the activity is paused
        Log.d(TAG, "onPause");
        stopBackgroundThread();
        super.onPause();
        mySensorManager.unregisterListener(this);
    }

    // this method is called to run the camera view in background
    protected void startBackgroundThread() {
        myBackgroundThread = new HandlerThread("Camera Background");
        myBackgroundThread.start();
        myBackgroundHandler = new Handler(myBackgroundThread.getLooper());
    }


    // this method is called to stop the camera view in background
    protected void stopBackgroundThread() {
        myBackgroundThread.quitSafely();
        try {
            myBackgroundThread.join();
            myBackgroundThread = null;
            myBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // This method is called to close the camera
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // this method requests the camera permissions, if the permissions are not available then it closes the application

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(PlaceScreenActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

    }


    // this listener takes care of the sensors registered and accessed by the sensor manager
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        float[] gravityData = new float[3];
        float[] magnetoData = new float[3];
        float[] rotationMatrix = new float[9];
        float[] iMatrix = new float[9];
        float[] orientation = new float[3];

        float[] rMat = new float[9];
        float[] rotVal= new float[3];
        public void onAccuracyChanged( Sensor sensor, int accuracy ) {}
        @Override
        public void onSensorChanged( SensorEvent event ) {
            float[] data;
            switch ( event.sensor.getType() ) {
                case Sensor.TYPE_GRAVITY:
                    gravityData = event.values.clone();
                    Log.d("garvity data frmgrsen", "1st "+String.valueOf(gravityData[0])+" 2nd"+String.valueOf(gravityData[1])+" 3rd"+String.valueOf(gravityData[2]));
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    gravityData = event.values.clone();
                    Log.d("garvity data accses", "1st "+String.valueOf(gravityData[0])+" 2nd"+String.valueOf(gravityData[1])+" 3rd"+String.valueOf(gravityData[2]));
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magnetoData = event.values.clone();
                    Log.d("magneto data", "1st "+String.valueOf(magnetoData[0])+" 2nd"+String.valueOf(magnetoData[1])+" 3rd"+String.valueOf(magnetoData[2]));
                    break;
                default: return;
            }


            if ( SensorManager.getRotationMatrix( rotationMatrix, iMatrix, gravityData, magnetoData ) ) {
                myAzimuth= (int) ( Math.toDegrees( SensorManager.getOrientation( rotationMatrix, orientation )[0] ) + 360 ) % 360;
                Log.d("mAzimuth", String.valueOf(myAzimuth));
            }
            else{
                Log.d("Not getting values", String.valueOf(myAzimuth));
            }
            myPressure = event.values[0];
            myAltitude = (int) (getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, myPressure));
            Log.d("altitude", String.valueOf(myAltitude));
        }
    };


    @Override
    public void onSensorChanged(SensorEvent event) {


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // this method sends the POST request with the collected tag's id, email address and the tag image with camera view in the background
    public void sendPOSTRequest(){
        StringRequest myStringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.web_api) + "placetag.php",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String MyResponse) {
                        Log.d("Response  ", MyResponse);
                        if(!MyResponse.isEmpty()){
                            if(MyResponse.contentEquals("0")){
                                Toast.makeText(getApplicationContext(), "Tag committed successfully", Toast.LENGTH_LONG).show();

                            }
                            else if(MyResponse.contentEquals("-1")) {
                                Toast.makeText(getApplicationContext(), "Some error occured while commiting tag", Toast.LENGTH_LONG).show();
                            }
                            else{
                                Log.d("Response for PlaceTag",MyResponse);
                            }
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
                param_map.put("email", emailAddress);
                param_map.put("tag_img", imageTagString);
                param_map.put("loc_long", currentLongitude);
                param_map.put("loc_lat", currentLatitude);
                param_map.put("orient_azimuth", String.valueOf(myAzimuth));
                param_map.put("orient_altitude", String.valueOf(myAltitude));
                Log.d("putting every placetag", "done");
                return param_map;
            }
        };
        WebApiServer.getInstance(getApplicationContext()).addToQueue(myStringRequest);

    }


}