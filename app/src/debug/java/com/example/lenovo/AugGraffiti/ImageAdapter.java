package com.example.lenovo.AugGraffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.util.List;

/**
 * Created by Lenovo on 10/11/2016.
 */
public class ImageAdapter extends BaseAdapter {

    List<String> urls;
    Context context;

    public ImageAdapter(Context context, List<String> urls) {
        this.context = context;
        this.urls = urls;
    }

   /* this method gets the view of the gdata in the specified position in a dataset */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(R.layout.photo_item, null);

        final ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.image_photo);

        //Makes a volley request to fetch the Image from the URL and load it in bitmap
        ImageRequest ir = new ImageRequest(urls.get(position), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imagePhoto.setImageBitmap(response);
            }
        }, 0, 0, null, null);
        WebApiServer.getInstance(context.getApplicationContext()).addToQueue(ir);


        return rootView;

    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return urls.size();
    }
}
