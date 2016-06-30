package rs.elfak.mosis.marko.discounthunt;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrentLocation {
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private double lat, lng;

    public CurrentLocation(LocationManager locationManager){
        mLocationManager = locationManager;
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
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

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lat", lat);
            jsonObject.put("lng", lng);
        }catch (JSONException ex) {}
        return jsonObject;
    }
}
