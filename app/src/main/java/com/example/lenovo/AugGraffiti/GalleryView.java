package com.example.lenovo.AugGraffiti;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryView extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery_view);

        // this creates a new list consisting of list of collected tag image urls
        ArrayList<String> list = getIntent().getStringArrayListExtra("list_of_urls");

        // this creates a gridview in which we display the collected tags
        final GridView gridview = (GridView) findViewById(R.id.gridview);

        // this displays all the images in the gallery using the image adapter
        gridview.setAdapter(new ImageAdapter(this, list));
    }
}


