package com.example.lenovo.AugGraffiti;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static android.hardware.SensorManager.getAltitude;

public class CollectScreenActivity extends AppCompatActivity implements SensorEventListener {
    private String collect_img;
    private final int CAMERA_REQUEST_CODE = 2222;
    private static final String TAG = "CollectScreenActivity";
    private TextureView textureView;
    private String cameraId;
    private ImageView imageView;
    private Button button;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Bitmap url;
    double currentAltitude=0;
    String tag_id=null;
    String tagImgUrl=null;
    String tagAzimuth=null;
    String tagAltitude=null;
    private int myAzimuth = 0;
    private SensorManager mySensorManager = null;
    private Sensor myGravity;
    private Sensor myAccelerometer;
    private Sensor myMagnetometer;
    private Sensor myPressureSensor;
    boolean havePressureSensor = false;
    boolean haveGravitySensor = false;
    boolean haveAccelerometerSensor = false;
    boolean haveMagnetometerSensor = false;
    private float pressure;
    private int altitude;
    private String emailAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_screen);

        //getting the email address from the mapscreen activity
        emailAddress=getIntent().getStringExtra("email");

        //getting the tag id from the mapscreen activity
        tag_id=getIntent().getStringExtra("tagId");

        //getting the altitude of the device from the mapscreen activity
        currentAltitude= Double.parseDouble(getIntent().getStringExtra("altitude"));
        Log.d("Altitude__from MAPS", String.valueOf(currentAltitude));

        // this is used to  set the view as texture view
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;

        // this calls the surface texture listener of the texture view and creates the view from back facing camera
        textureView.setSurfaceTextureListener(textureListener);

        // this call implements the image view of the retrieved tag
        imageView = (ImageView) findViewById(R.id.tagimage);

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

        // if the gravity sensor is available then unregister the accelerometer sensor
        if( this.haveGravitySensor ) {
            this.mySensorManager.unregisterListener(this.mSensorEventListener, this.myAccelerometer);
        }


        // this sends a POST request to retrieve the image of the tag placed at a position and responsds with tag url,
        callPostRequest();

        /* Clicking the collect button below listener is called that takes the combined image of the tag
           along with camera view in the back
        */

        button= (Button) findViewById(R.id.Collect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Bitmap tag_bmp = scaleBitmap(url, 300, 300);
                Bitmap camera_bmp =  textureView.getBitmap();
                Bitmap save = overlay(camera_bmp ,tag_bmp );
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                save.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                byte[] byteFormat = outputStream.toByteArray();
                String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
                collect_img = imgString; //64 base encoded image is stored in the collect_img
                Log.d("TAG", collect_img);

                sendPOSTRequest();
                Log.d("hi", collect_img);
                Log.d("hi", tag_id);
                Log.d("hi", emailAddress);
                }
        });
    }


    // this listener enables the view of back facing camera and closes the camera once the surface is destroyed
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
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
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.d(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CollectScreenActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Camera opened");
    }


    // this callback method starts the preview of the back facing camera
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
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
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
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
                    Toast.makeText(CollectScreenActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    // this method updates the camera preview and helps in continuous streaming of the back facing camera
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {

        // This method starts a background thread and continuously streams the backfacing camera view
        super.onResume();
        Log.d(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override

    protected void onPause() {

        // This method stops the camera view and unregisters the sensors when the activity is paused
        super.onPause();
        stopBackgroundThread();
        mySensorManager.unregisterListener(this);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // this method requests the camera permissions, if the permissions are not available then it closes the application

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                Toast.makeText(CollectScreenActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is used to to store the back facing camera view into a bitmap
        if (data != null && requestCode == CAMERA_REQUEST_CODE) {
            Bitmap backCamera_bmp = (Bitmap) data.getExtras().get("data");
            textureView.getBitmap(backCamera_bmp);
        }
    }


    // this method is called to run the camera view in background
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Cameraview in Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }


    // this method is called to stop the camera view in background
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
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


    // this method is called to overlay two bitmaps with each of the bitmaps as arguments
    private Bitmap overlay(Bitmap back, Bitmap front) {
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
        Canvas canvas = new Canvas(result);
        int widthBack = back.getWidth();
        int widthFront = front.getWidth();
        int heightBack = back.getHeight();
        int heightFront = front.getHeight();
        float width = (widthBack - widthFront) / 2;
        float height = (heightBack - heightFront)/ 2;
        canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, width, height, null);
        return result;
    }


    // ths method is called to scale a bitmap for the desired width and height
    public static Bitmap scaleBitmap(Bitmap bitmap, int desiredWidth, int desiredHeight) {
        Bitmap output = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix matrix = new Matrix();
        matrix.setScale((float) desiredWidth / bitmap.getWidth(), (float) desiredHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, matrix, new Paint());
        return output;
    }


    // this method is called to send the POST Request to find the collected tag and retrieves the url, azimuth and altitude of the tag
    public void callPostRequest()
    {
        Log.d("Altitude_calling", "call post req");
        StringRequest myStringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.web_api) + "findtag.php",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String MyResponse) {
                        Log.d("Response", MyResponse);
                        if(!MyResponse.isEmpty()){
                            String[] arr = MyResponse.split(",");
                            tagImgUrl=arr[0];
                            tagAzimuth=arr[1];
                            tagAltitude=arr[2];

                            // This sets an image request to display the image from the tag url
                            ImageRequest ir = new ImageRequest(tagImgUrl, new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    url = response;
                                    imageView.setImageBitmap(url);
                                }
                            }, 0, 0, null, null);
                            WebApiServer.getInstance(getApplicationContext()).addToQueue(ir);
                            Log.d("tagImg", tagImgUrl);
                            Log.d("azimuth_ofTag",tagAzimuth);
                            Log.d("altitude_oftag", tagAltitude);

                        }
                        else{
                            return;
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //when there is an error response then this method is called

                Log.e("Response", String.valueOf(error));
                Toast.makeText(getApplicationContext(), "Error getting a response from Server. Please try later", Toast.LENGTH_LONG).show();


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param_map = new HashMap<>();
                param_map.put("tag_id", tag_id);
                Log.d("putting tag", "done");
                return param_map;
            }
        };
        //Adding the StringRequest to the RequestQueue
        WebApiServer.getInstance(getApplicationContext()).addToQueue(myStringRequest);

    }


    // this listener takes care of the sensors registered and accessed by the sensor manager
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        float[] gravityData = new float[3];
        float[] magnetoData = new float[3];
        float[] rotationMatrix = new float[9];
        float[] iMatrix = new float[9];
        float[] orientation = new float[3];
        public void onAccuracyChanged( Sensor sensor, int accuracy ) {}
        @Override
        public void onSensorChanged( SensorEvent event ) {
            float[] data;
            switch ( event.sensor.getType() ) {
                case Sensor.TYPE_GRAVITY:
                    gravityData = event.values.clone();
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    gravityData = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magnetoData = event.values.clone();
                    break;
                default: return;
            }
            if ( SensorManager.getRotationMatrix( rotationMatrix, iMatrix, gravityData, magnetoData ) ) {
                myAzimuth= (int) ( Math.toDegrees( SensorManager.getOrientation( rotationMatrix, orientation )[0] ) + 360 ) % 360;
                Log.d("mAzimuth", String.valueOf(myAzimuth));
                if(tagAzimuth!=null){
                    Log.d("tagAzimuth", String.valueOf(tagAzimuth));
                    if(myAzimuth<=(Integer.parseInt(tagAzimuth)+180) && myAzimuth>=(Integer.parseInt(tagAzimuth)-180)){
                        Log.d("azimuth in Range", String.valueOf(Integer.parseInt(tagAzimuth)));
                        imageView.setVisibility(View.VISIBLE);
                        button.setVisibility(View.VISIBLE);

                    }
                    else{
                        Log.d("out of range", tagAzimuth);
                        imageView.setVisibility(View.GONE);
                        button.setVisibility(View.GONE);
                    }
                }}
            else{
                Log.d("Not getting values", String.valueOf(myAzimuth));
            }

            pressure = event.values[0];
            altitude = (int) (getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure));
            Log.d("altitude_from_pressure", String.valueOf(altitude));
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
        StringRequest myStringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.web_api) + "collecttag.php",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String MyResponse) {
                        Log.d("Response  ", MyResponse);
                        if(!MyResponse.isEmpty()){
                            if(MyResponse.contentEquals("0")){
                                Toast.makeText(getApplicationContext(), "Tag collected successfully", Toast.LENGTH_LONG).show();

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
                param_map.put("tag_id", tag_id);
                param_map.put("collect_img",collect_img);
                Log.d("collectingtag", "done");
                return param_map;
            }
        };
        WebApiServer.getInstance(getApplicationContext()).addToQueue(myStringRequest);

    }
}


