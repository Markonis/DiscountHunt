package rs.elfak.mosis.marko.discounthunt;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.volley.Response;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrentLocation {
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private Response.Listener<Location> listener;
    private LatLng latLng;

    public CurrentLocation(LocationManager locationManager){
        latLng = new LatLng(0, 0);
        mLocationManager = locationManager;
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if(listener != null){
                    listener.onResponse(location);
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
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, mLocationListener);
        }catch (SecurityException ex) {
            System.out.println("Error!");
        }
    }

    public void setListener(Response.Listener<Location> listener) {
        this.listener = listener;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lat", latLng.latitude);
            jsonObject.put("lng", latLng.longitude);
        }catch (JSONException ex) {}
        return jsonObject;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
