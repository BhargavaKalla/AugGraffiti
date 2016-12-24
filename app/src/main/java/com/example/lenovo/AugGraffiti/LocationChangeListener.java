package com.example.lenovo.AugGraffiti;


import com.google.android.gms.maps.model.Marker;

/* LocationChangeListener is an Interface which implements OnLocationChange method.
This method is initialized whenever there is a change in Location caught by LocationListener in
LocationTracker service. Then, this change in location is monitored in MapActivity to update
the current location marker(P_TAG) of the user.
*/
public interface LocationChangeListener {
    void onLocationChange(Double lat, Double lng);


}
