package com.example.lenovo.AugGraffiti;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/*
This singleton class is used to maintain a single RequestQueue and avoids
 multiple queues for multiple requests to the Web_ApI server.
 */
public class WebApiServer {
    private static Context context;
    private RequestQueue requestQueue;
    private static WebApiServer wInstance;

    //Set the current context and call getRequestQueue method
    public WebApiServer(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();

    }

    /*
    Set the RequestQueue with the current Context if it is not set or
    else return the RequestQueue
     */

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    /*Check whether the instance is already been created and then returns
    the current Instance
    */
    public static synchronized WebApiServer getInstance(Context context) {
        if (wInstance == null) {
            wInstance = new WebApiServer(context);

        }
        return wInstance;
    }

    public <T> void addToQueue(Request<T> request) {
        requestQueue.add(request);

    }

}
