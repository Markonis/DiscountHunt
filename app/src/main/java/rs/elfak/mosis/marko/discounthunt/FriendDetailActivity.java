package rs.elfak.mosis.marko.discounthunt;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserEndpoint;

public class FriendDetailActivity extends AppCompatActivity {

    private ImageView mPhoto;
    private TextView mName, mPhone, mRank;
    private JSONObject userJsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        mPhoto = (ImageView) findViewById(R.id.photo);
        mName = (TextView) findViewById(R.id.name);
        mPhone = (TextView) findViewById(R.id.phone);
        mRank = (TextView) findViewById(R.id.rank);

        loadData();
    }

    private void loadData() {
        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("id");
        UserEndpoint userEndpoint = new UserEndpoint();
        userEndpoint.get(id,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        userJsonObject = response;
                        updateView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
    }

    private void updateView() {
        try {
            mName.setText(userJsonObject.getString("first_name") +
                    " " + userJsonObject.getString("last_name"));
            mPhone.setText(userJsonObject.getString("phone"));
            mRank.setText(String.valueOf(userJsonObject.getInt("rank")));
            if(!userJsonObject.isNull("photo")){
                JSONObject photoJsonObject = userJsonObject.getJSONObject("photo");
                Bitmap bitmap = Camera.decodeBase64(photoJsonObject.getString("data"));
                mPhoto.setBackground(new BitmapDrawable(getResources(),bitmap));
            }
        }catch (JSONException ex) {}
    }
}
