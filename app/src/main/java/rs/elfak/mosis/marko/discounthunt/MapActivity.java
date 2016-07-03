package rs.elfak.mosis.marko.discounthunt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountSearchEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserSearchEndpoint;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private FloatingActionButton fab;
    private CurrentLocation mCurrentLocation;
    private TimerTask refreshTimerTask;
    private Timer refreshTimer;
    private JSONArray mDiscounts;
    private JSONArray mUsers;
    private ArrayList<Marker> discountMarkers, userMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        discountMarkers = new ArrayList<>();
        userMarkers = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        fab = (FloatingActionButton) findViewById(R.id.create_discount);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateDiscountActivity();
            }
        });

        // Current location
        mCurrentLocation = new CurrentLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        mCurrentLocation.setListener(new Response.Listener<Location>() {
            @Override
            public void onResponse(Location location) {
                moveCameraToLocation(location);
            }
        });

        startRefreshTimer();
    }

    private void startRefreshTimer() {
        refreshTimerTask = new TimerTask() {
            @Override
            public void run() {
            try {
                JSONObject searchJsonObject = new JSONObject();
                searchJsonObject.put("query", "");
                DiscountSearchEndpoint discountSearchEndpoint = new DiscountSearchEndpoint();
                discountSearchEndpoint.post(searchJsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                            updateDiscountMarkers(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }
                );

                JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
                searchJsonObject = new JSONObject();
                searchJsonObject.put("friends_with", userJsonObject.getInt("id"));
                UserSearchEndpoint userSearchEndpoint = new UserSearchEndpoint();
                userSearchEndpoint.post(searchJsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                updateUserMarkers(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }
                );

            }catch (JSONException ex) {}
            }
        };
        refreshTimer = new Timer();
        refreshTimer.schedule(refreshTimerTask, 1000, 10000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.show_discounts_list:
                startDiscountsListActivity();
                return true;
            case R.id.show_add_friend:
                startAddFriendActivity();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void moveCameraToLocation(Location location) {
        if(mMap != null){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    private void updateDiscountMarkers(JSONObject searchJsonObject) {
        try {
            String result = searchJsonObject.getString("result");
            mDiscounts = new JSONArray(result);

            for(Marker marker : discountMarkers){
                marker.remove();
            }

            if(mMap != null) {
                for (int i = 0; i < mDiscounts.length(); i++) {
                    MarkerOptions markerOptions = discountMarkerOptions(mDiscounts.getJSONObject(i));
                    if(markerOptions != null) {
                        discountMarkers.add(mMap.addMarker(markerOptions));
                    }
                }
            }

        }catch (JSONException ex) {}
    }

    private MarkerOptions discountMarkerOptions(JSONObject discountJsonObject) {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            JSONObject locationJsonObject = discountJsonObject.getJSONObject("location");
            markerOptions.position(new LatLng(
                    locationJsonObject.getDouble("lat"),
                    locationJsonObject.getDouble("lng")));
            if(discountJsonObject.has("photo")) {
                JSONObject photoJsonObject = discountJsonObject.getJSONObject("photo");
                Bitmap bitmap = Camera.decodeBase64(photoJsonObject.getString("data"));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }
            return markerOptions;
        }catch (JSONException ex) {
            return null;
        }
    }

    private void updateUserMarkers(JSONObject searchJsonObject) {
        try {
            String result = searchJsonObject.getString("result");
            mUsers = new JSONArray(result);

            for(Marker marker : userMarkers){
                marker.remove();
            }

            if(mMap != null) {
                for (int i = 0; i < mUsers.length(); i++) {
                    MarkerOptions markerOptions = userMarkerOptions(mUsers.getJSONObject(i));
                    if(markerOptions != null) {
                        userMarkers.add(mMap.addMarker(markerOptions));
                    }
                }
            }

        }catch (JSONException ex) {}
    }

    private MarkerOptions userMarkerOptions(JSONObject userJsonObject) {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            JSONObject locationJsonObject = userJsonObject.getJSONObject("location");
            markerOptions.position(new LatLng(
                    locationJsonObject.getDouble("lat"),
                    locationJsonObject.getDouble("lng")));
            if(userJsonObject.has("photo")) {
                JSONObject photoJsonObject = userJsonObject.getJSONObject("photo");
                Bitmap bitmap = Camera.decodeBase64(photoJsonObject.getString("data"));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }
            return markerOptions;
        }catch (JSONException ex) {
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng latLng = mCurrentLocation.getLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRefreshTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshTimer.purge();
    }

    private void startCreateDiscountActivity() {
        Intent intent = new Intent(getApplicationContext(), CreateDiscountActivity.class);
        startActivity(intent);
    }

    private void startDiscountsListActivity() {
        Intent intent = new Intent(getApplicationContext(), DiscountsListActivity.class);
        startActivity(intent);
    }

    private void startAddFriendActivity() {
        Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
        startActivity(intent);
    }
}
