package rs.elfak.mosis.marko.discounthunt;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuInflater;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountSearchEndpoint;

public class DiscountsListActivity extends AppCompatActivity {

    private FloatingActionButton mFab;
    private Button mSearch;
    private TextView mCountText, mRadiusText;
    private EditText mQueryText;
    private ListView mList;
    private JSONArray mDiscounts;

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
        mCountText = (TextView) findViewById(R.id.discounts_list_count_text);
        mRadiusText = (TextView) findViewById(R.id.discounts_list_radius_text);

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
            String discountStr = mDiscounts.getJSONObject(position).toString();
            Intent intent = new Intent(getApplicationContext(), DiscountDetailActivity.class);
            intent.putExtra("discount", discountStr);
            startActivity(intent);
        }catch (JSONException ex){}
    }

    private void search() {
        String query = mQueryText.getText().toString();
        JSONObject searchJsonObject = new JSONObject();
        try {
            searchJsonObject.put("query", query);
            DiscountSearchEndpoint endpoint = new DiscountSearchEndpoint();
            endpoint.post(searchJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject searchJsonObject) {
                        populateList(searchJsonObject);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );
        }catch (JSONException ex) {

        }
    }

    private void populateList(JSONObject searchJsonObject) {
        ArrayList<String> items = new ArrayList<>();
        try {
            String result = searchJsonObject.getString("result");
            mDiscounts = new JSONArray(result);
            for(int i = 0; i < mDiscounts.length(); i++){
                JSONObject discountJsonObject = mDiscounts.getJSONObject(i);
                String itemStr = discountJsonObject.getString("title");
                items.add(itemStr);
            }
        } catch (JSONException e) {}
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        mList.setAdapter(itemsAdapter);
    }
}
