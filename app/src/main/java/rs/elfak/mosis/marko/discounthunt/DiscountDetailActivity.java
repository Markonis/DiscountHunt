package rs.elfak.mosis.marko.discounthunt;

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

import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountEndpoint;

public class DiscountDetailActivity extends AppCompatActivity {

    private JSONObject discountJsonObject;
    private TextView mTitle, mDescription, mVotes, mAuthor;
    private ImageButton mVoteUp;
    private ImageView mPhoto, mAuthorPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_detail);

        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.description);
        mVotes = (TextView) findViewById(R.id.votes);
        mAuthor = (TextView) findViewById(R.id.author);
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
            if(discountJsonObject.has("photo")){
                JSONObject photoJsonObject = discountJsonObject.getJSONObject("photo");
                Bitmap bitmap = Camera.decodeBase64(photoJsonObject.getString("data"));
                mPhoto.setBackground(new BitmapDrawable(getResources(),bitmap));
            }

            mTitle.setText(discountJsonObject.getString("title") + " - " + "$" + discountJsonObject.getDouble("price"));
            mDescription.setText(discountJsonObject.getString("description"));
            mVotes.setText(String.valueOf(discountJsonObject.getInt("votes")));
            JSONObject userJsonObject = discountJsonObject.getJSONObject("user");
            mAuthor.setText(userJsonObject.getString("first_name") + " " + userJsonObject.getString("last_name"));

            if(userJsonObject.has("photo")){
                JSONObject photoJsonObject = userJsonObject.getJSONObject("photo");
                Bitmap bitmap = Camera.decodeBase64(photoJsonObject.getString("data"));
                mAuthorPhoto.setBackground(new BitmapDrawable(getResources(),bitmap));
            }

        }catch (JSONException ex){}
    }

    private void voteUp() {
        try {
            int id = discountJsonObject.getInt("id");
            int votes = discountJsonObject.getInt("votes");
            discountJsonObject.put("votes", votes + 1);
            DiscountEndpoint discountEndpoint = new DiscountEndpoint();

            discountEndpoint.put(id, discountJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            updateView();
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
}
