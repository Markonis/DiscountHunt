package rs.elfak.mosis.marko.discounthunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.FriendsRankingEndpoint;

public class FriendsRankingActivity extends AppCompatActivity {

    private ListView mList;
    private ArrayList<String> mUserNames;
    private ArrayList<JSONObject> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_ranking);

        mUserNames = new ArrayList<>();
        mUsers = new ArrayList<>();
        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startFriendDetailActivity(position);
            }
        });

        loadRanking();
    }

    private void startFriendDetailActivity(int position) {
        try {
            int id = mUsers.get(position).getInt("id");
            Intent intent = new Intent(getApplicationContext(), FriendDetailActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }catch (JSONException ex){}
    }

    private void loadRanking() {
        try {
            FriendsRankingEndpoint endpoint = new FriendsRankingEndpoint();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id",
                    DiscountHunt.currentSession.getJSONObject("user").getInt("id"));
            endpoint.post(jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            populateList(response);
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

    private void populateList(JSONObject searchJsonObject) {
        try {
            String resultStr = searchJsonObject.getString("result");
            JSONArray jsonArray = new JSONArray(resultStr);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject userJsonObject = jsonArray.getJSONObject(i);
                mUsers.add(userJsonObject);
                String userStr = (i + 1) + ". " + userJsonObject.getString("first_name") + " " +
                        userJsonObject.getString("last_name") + " (" + userJsonObject.getInt("rank") + ")";
                mUserNames.add(userStr);
                ArrayAdapter<String> itemsAdapter =
                        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mUserNames);
                mList.setAdapter(itemsAdapter);
            }
        }catch (JSONException ex){}
    }
}
