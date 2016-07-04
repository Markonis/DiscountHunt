package rs.elfak.mosis.marko.discounthunt;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountVoteEndpoint;

public class DiscountDetailActivity extends AppCompatActivity {

    private JSONObject discountJsonObject;
    private TextView mTitle, mDescription, mVotes, mAuthorName, mPrice;
    private ImageButton mVoteUp;
    private ImageView mPhoto, mAuthorPhoto;
    private View mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_detail);

        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.description);
        mPrice = (TextView) findViewById(R.id.price);
        mVotes = (TextView) findViewById(R.id.votes);
        mAuthorName = (TextView) findViewById(R.id.author_name);
        mAuthor = findViewById(R.id.author);
        mPhoto = (ImageView) findViewById(R.id.photo);
        mAuthorPhoto = (ImageView) findViewById(R.id.author_photo);
        mVoteUp = (ImageButton) findViewById(R.id.vote_up);
        mVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voteUp();
            }
        });

        loadData();

        mAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFriendDetailActivity();
            }
        });
    }

    private void loadData() {
        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("id");
        DiscountEndpoint endpoint = new DiscountEndpoint();
        endpoint.get(id,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        discountJsonObject = response;
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
            if(!discountJsonObject.isNull("photo")){
                JSONObject photoJsonObject = discountJsonObject.getJSONObject("photo");
                Bitmap bitmap = Camera.decodeBase64(photoJsonObject.getString("data"));
                mPhoto.setBackground(new BitmapDrawable(getResources(),bitmap));
            }

            mTitle.setText(discountJsonObject.getString("title"));
            mDescription.setText(discountJsonObject.getString("description"));
            mVotes.setText(String.valueOf(discountJsonObject.getInt("votes")));
            NumberFormat currency = NumberFormat.getCurrencyInstance();
            mPrice.setText(currency.format(discountJsonObject.getDouble("price")));
            JSONObject userJsonObject = discountJsonObject.getJSONObject("user");
            mAuthorName.setText(userJsonObject.getString("first_name") + " " + userJsonObject.getString("last_name"));

            if(!userJsonObject.isNull("photo")){
                JSONObject photoJsonObject = userJsonObject.getJSONObject("photo");
                Bitmap bitmap = Camera.decodeBase64(photoJsonObject.getString("data"));
                mAuthorPhoto.setBackground(new BitmapDrawable(getResources(),bitmap));
            }

        }catch (JSONException ex){}
    }

    private void voteUp() {
        try {
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            DiscountVoteEndpoint endpoint = new DiscountVoteEndpoint();
            JSONObject voteJsonObject = new JSONObject();
            voteJsonObject.put("discount_id", discountJsonObject.getInt("id"));
            voteJsonObject.put("user_id", userJsonObject.getInt("id"));
            endpoint.post(voteJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            voted();
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

    private void voted(){
        try {
            int votes = discountJsonObject.getInt("votes");
            discountJsonObject.put("votes", votes + 1);
            updateView();
        }catch (JSONException ex){}
    }

    private void startFriendDetailActivity() {
        try {
            JSONObject userJsonObject = discountJsonObject.getJSONObject("user");
            Intent intent = new Intent(getApplicationContext(), FriendDetailActivity.class);
            intent.putExtra("id", userJsonObject.getInt("id"));
            startActivity(intent);
        }catch (JSONException ex){}
    }
}
