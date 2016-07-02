package rs.elfak.mosis.marko.discounthunt;

import android.content.Context;
import android.content.Intent;
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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private FloatingActionButton fab;
    private CurrentLocation mCurrentLocation;
    private TimerTask refreshTimerTask;
    private Timer refreshTimer;
    private JSONArray mDiscounts;
    private ArrayList<Marker> discountMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        discountMarkers = new ArrayList<>();

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

        // Discounts refresh timer
        refreshTimerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    JSONObject searchJsonObject = new JSONObject();
                    searchJsonObject.put("query", "");
                    DiscountSearchEndpoint endpoint = new DiscountSearchEndpoint();
                    endpoint.post(searchJsonObject,
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

    private void startDiscountsListActivity() {
        Intent intent = new Intent(getApplicationContext(), DiscountsListActivity.class);
        startActivity(intent);
    }

    private void startAddFriendActivity() {
        Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
        startActivity(intent);
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
                        discountMarkers.add(mMap.addMarker(
                                discountMarkerOptions(mDiscounts.getJSONObject(i))));
                    }
                }
            }

        }catch (JSONException ex) {

        }
    }

    private MarkerOptions discountMarkerOptions(JSONObject discountJsonObject) {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            JSONObject locationJsonObject = discountJsonObject.getJSONObject("location");
            markerOptions.position(new LatLng(
                    locationJsonObject.getDouble("lat"),
                    locationJsonObject.getDouble("lng")));
            return markerOptions;
        }catch (JSONException ex) {
            return null;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng latLng = mCurrentLocation.getLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void startCreateDiscountActivity() {
        Intent intent = new Intent(getApplicationContext(), CreateDiscountActivity.class);
        startActivity(intent);
    }
}
