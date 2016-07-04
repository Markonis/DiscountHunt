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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountSearchEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserLocationChangeEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserSearchEndpoint;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final float ZOOM = 14;
    private static final int MARKER_SIZE = 96;
    private static final long REFRESH_INTERVAL = 5000;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private FloatingActionButton fab;
    private CurrentLocation mCurrentLocation;
    private TimerTask refreshTimerTask;
    private Timer refreshTimer;
    private ArrayList<JSONObject> mDiscounts, mUsers;
    private ArrayList<Marker> discountMarkers, userMarkers;
    private Marker mUserMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        discountMarkers = new ArrayList<>();
        userMarkers = new ArrayList<>();
        mDiscounts = new ArrayList<>();
        mUsers = new ArrayList<>();

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
                updateUserMarker();
                updateUserLocation();
                moveCameraToLocation();
            }
        });

        startRefreshTimer();
    }

    private void startRefreshTimer() {
        refreshTimerTask = new TimerTask() {
            @Override
            public void run() {
                updateUserLocation();
                searchDiscounts();
                searchUsers();
            }
        };
        refreshTimer = new Timer();
        refreshTimer.schedule(refreshTimerTask, 1000, REFRESH_INTERVAL);
    }

    private void searchDiscounts() {
        try {
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            JSONObject searchJsonObject = new JSONObject();
            searchJsonObject.put("query", "");
            searchJsonObject.put("by_friends_of", userJsonObject.getInt("id"));
            searchJsonObject.put("location_attributes", locationSearchAttributes());
            DiscountSearchEndpoint discountSearchEndpoint = new DiscountSearchEndpoint();
            discountSearchEndpoint.post(searchJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            loadDiscounts(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
        }catch (JSONException ex){}
    }

    private void loadDiscounts(JSONObject searchJsonObject) {
        try {
            String resultStr = searchJsonObject.getString("result");
            JSONArray resultJsonArray = new JSONArray(resultStr);
            for(int i = 0; i < resultJsonArray.length(); i++){
                loadDiscount(resultJsonArray.getJSONObject(i).getInt("id"));
            }
        }catch (JSONException ex){}
    }

    private void loadDiscount(int id) {
        DiscountEndpoint endpoint = new DiscountEndpoint();
        endpoint.get(id,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MarkerOptions markerOptions = discountMarkerOptions(response);
                        if(markerOptions != null) {
                            clearDiscount(response);
                            mDiscounts.add(response);
                            discountMarkers.add(mMap.addMarker(markerOptions));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
    }

    private void clearDiscount(JSONObject discountJsonObject) {
        try {
            int index = -1;
            for(int i = 0; i < mDiscounts.size(); i++){
                if(mDiscounts.get(i).getInt("id") == discountJsonObject.getInt("id")){
                    index = i;
                }
            }
            if(index > -1) {
                mDiscounts.remove(index);
                discountMarkers.get(index).remove();
                discountMarkers.remove(index);
            }
        }catch (JSONException ex){}
    }

    private MarkerOptions discountMarkerOptions(JSONObject discountJsonObject) {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            JSONObject locationJsonObject = discountJsonObject.getJSONObject("location");
            markerOptions.title(discountJsonObject.getString("title"));
            markerOptions.position(new LatLng(
                    locationJsonObject.getDouble("lat"),
                    locationJsonObject.getDouble("lng")));
            if(!discountJsonObject.isNull("photo")) {
                JSONObject photoJsonObject = discountJsonObject.getJSONObject("photo");
                Bitmap bitmap = resizedBitmap(
                        Camera.decodeBase64(photoJsonObject.getString("data")));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                markerOptions.anchor(0.5f, 0.5f);
            }else{
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
            return markerOptions;
        }catch (JSONException ex) {
            return null;
        }
    }

    private void searchUsers() {
        try {
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            JSONObject searchJsonObject = new JSONObject();
            searchJsonObject.put("friends_with", userJsonObject.getInt("id"));
            searchJsonObject.put("location_attributes", locationSearchAttributes());
            UserSearchEndpoint userSearchEndpoint = new UserSearchEndpoint();
            userSearchEndpoint.post(searchJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            loadUsers(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {}
                    });
        }catch (JSONException ex) {}
    }

    private void loadUsers(JSONObject searchJsonObject) {
        try {
            String resultStr = searchJsonObject.getString("result");
            JSONArray resultJsonArray = new JSONArray(resultStr);
            for(int i = 0; i < resultJsonArray.length(); i++){
                loadUser(resultJsonArray.getJSONObject(i).getInt("id"));
            }
        }catch (JSONException ex){}
    }

    private void loadUser(int id) {
        UserEndpoint endpoint = new UserEndpoint();
        endpoint.get(id,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MarkerOptions markerOptions = userMarkerOptions(response);
                        if(markerOptions != null) {
                            clearUser(response);
                            mUsers.add(response);
                            userMarkers.add(mMap.addMarker(markerOptions));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
    }

    private void clearUser(JSONObject userJsonObject) {
        try {
            int index = -1;
            for(int i = 0; i < mUsers.size(); i++){
                if(mUsers.get(i).getInt("id") == userJsonObject.getInt("id")){
                    index = i;
                }
            }
            if(index > -1) {
                mUsers.remove(index);
                userMarkers.get(index).remove();
                userMarkers.remove(index);
            }
        }catch (JSONException ex){}
    }

    private MarkerOptions userMarkerOptions(JSONObject userJsonObject) {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            JSONObject locationJsonObject = userJsonObject.getJSONObject("location");
            markerOptions.title(userJsonObject.getString("first_name"));
            markerOptions.position(new LatLng(
                    locationJsonObject.getDouble("lat"),
                    locationJsonObject.getDouble("lng")));
            if(!userJsonObject.isNull("photo")) {
                JSONObject photoJsonObject = userJsonObject.getJSONObject("photo");
                Bitmap bitmap = resizedBitmap(
                        Camera.decodeBase64(photoJsonObject.getString("data")));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                markerOptions.anchor(0.5f, 0.5f);
            }else{
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            return markerOptions;
        }catch (JSONException ex) {
            return null;
        }
    }

    private JSONObject locationSearchAttributes() {
        JSONObject jsonObject = mCurrentLocation.toJSONObject();
        try {
            JSONObject settingJsonObject =
                    DiscountHunt.currentSession.getJSONObject("user").getJSONObject("setting");
            jsonObject.put("radius", settingJsonObject.getDouble("search_radius"));
        }catch (JSONException ex){}
        return jsonObject;
    }

    private void updateUserLocation() {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            jsonObject.put("user_id", userJsonObject.getInt("id"));
            jsonObject.put("location_attributes", mCurrentLocation.toJSONObject());
            UserLocationChangeEndpoint endpoint = new UserLocationChangeEndpoint();
            endpoint.post(jsonObject,
                    new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );
        }catch (JSONException ex){}
    }

    private void updateUserMarker() {
        mUserMarker.setPosition(mCurrentLocation.getLatLng());
    }

    private Bitmap resizedBitmap(Bitmap bitmap){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, MARKER_SIZE, MARKER_SIZE, false);
        return resizedBitmap;
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

    private void startDiscountDetailActivity(int index) {
        try {
            JSONObject discountJsonObject = mDiscounts.get(index);
            Intent intent = new Intent(getApplicationContext(), DiscountDetailActivity.class);
            intent.putExtra("id", discountJsonObject.getInt("id"));
            startActivity(intent);
        }catch (JSONException ex) {}
    }

    private void startFriendDetailActivity(int index) {
        try {
            JSONObject userJsonObject = mUsers.get(index);
            Intent intent = new Intent(getApplicationContext(), FriendDetailActivity.class);
            intent.putExtra("id", userJsonObject.getInt("id"));
            startActivity(intent);
        }catch (JSONException ex) {}
    }

    private void startSettingActivity() {
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        startActivity(intent);
    }

    private void moveCameraToLocation() {
        if(mMap != null){
            CameraPosition position = new CameraPosition.Builder()
                    .target(mCurrentLocation.getLatLng())
                    .zoom(ZOOM).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }
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
            case R.id.show_setting:
                startSettingActivity();
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = mCurrentLocation.getLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setOnMarkerClickListener(this);

        // Add user location marker
        MarkerOptions userMarkerOptions = new MarkerOptions();
        userMarkerOptions.position(latLng);
        mUserMarker = mMap.addMarker(userMarkerOptions);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        for(int i = 0; i < discountMarkers.size(); i++){
            if(discountMarkers.get(i).equals(marker)){
                startDiscountDetailActivity(i);
                return true;
            }
        }

        for(int i = 0; i < userMarkers.size(); i++){
            if(userMarkers.get(i).equals(marker)) {
                startFriendDetailActivity(i);
                return true;
            }
        }

        return false;
    }
}
