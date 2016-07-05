package rs.elfak.mosis.marko.discounthunt;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountSearchEndpoint;

public class DiscountsListActivity extends AppCompatActivity {

    private FloatingActionButton mFab;
    private Button mSearch;
    private TextView mCountText, mRadiusText;
    private EditText mQueryText;
    private ListView mList;
    private JSONArray mDiscounts;
    private CurrentLocation mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discounts_list);

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startDiscountDetailActivity(position);
            }
        });

        mQueryText = (EditText) findViewById(R.id.query_text);
        mCountText = (TextView) findViewById(R.id.count);
        mRadiusText = (TextView) findViewById(R.id.radius);

        mFab = (FloatingActionButton) findViewById(R.id.create_discount);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateDiscountActivity();
            }
        });

        mSearch = (Button) findViewById(R.id.search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        mCurrentLocation = new CurrentLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        search();
    }

    @Override
    protected void onResume() {
        super.onResume();
        search();
    }

    private void startCreateDiscountActivity() {
        Intent intent = new Intent(getApplicationContext(), CreateDiscountActivity.class);
        startActivity(intent);
    }

    private void startDiscountDetailActivity(int position) {
        try{
            int id = mDiscounts.getJSONObject(position).getInt("id");
            Intent intent = new Intent(getApplicationContext(), DiscountDetailActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }catch (JSONException ex){}
    }

    private void search() {
        String query = mQueryText.getText().toString();
        JSONObject searchJsonObject = new JSONObject();
        try {
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            searchJsonObject.put("query", query);
            searchJsonObject.put("location_attributes", locationSearchAttributes());
            searchJsonObject.put("by_friends_of", userJsonObject.getInt("id"));
            DiscountSearchEndpoint endpoint = new DiscountSearchEndpoint();
            endpoint.post(searchJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject searchJsonObject) {
                        updateView(searchJsonObject);
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

    private JSONObject locationSearchAttributes() {
        JSONObject jsonObject = mCurrentLocation.toJSONObject();
        try {
            JSONObject settingJsonObject =
                    DiscountHunt.currentSession.getJSONObject("user").getJSONObject("setting");
            jsonObject.put("radius", settingJsonObject.getDouble("search_radius"));
        }catch (JSONException ex){}
        return jsonObject;
    }

    private void updateView(JSONObject searchJsonObject) {
        ArrayList<String> items = new ArrayList<>();
        try {
            String result = searchJsonObject.getString("result");
            mDiscounts = new JSONArray(result);
            for(int i = 0; i < mDiscounts.length(); i++){
                JSONObject discountJsonObject = mDiscounts.getJSONObject(i);
                String itemStr = discountJsonObject.getString("title");
                items.add(itemStr);
            }

            String countStr = "Showing " + mDiscounts.length() + " discounts";
            mCountText.setText(countStr);

            double radius = DiscountHunt.currentSession.getJSONObject("user")
                    .getJSONObject("setting").getDouble("search_radius");
            String radiusStr = "in " + radius + " degrees radius";
            mRadiusText.setText(radiusStr);

        } catch (JSONException e) {}
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        mList.setAdapter(itemsAdapter);
    }
}
