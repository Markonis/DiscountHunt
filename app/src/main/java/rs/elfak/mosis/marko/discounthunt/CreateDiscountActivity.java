package rs.elfak.mosis.marko.discounthunt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.DiscountEndpoint;

public class CreateDiscountActivity extends AppCompatActivity {

    private Button mCreateButton;
    private EditText mTitle, mDescription, mPrice;
    private Spinner mCategory;
    private CurrentLocation mCurrentLocation;
    private Camera mCamera;
    private ImageButton mTakePhoto;
    private String mPhotoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_discount);
        initCategory();
        mCamera = new Camera(this);
        mCurrentLocation = new CurrentLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        mCreateButton = (Button) findViewById(R.id.create_discount);
        mTitle = (EditText) findViewById(R.id.title);
        mDescription = (EditText) findViewById(R.id.description);
        mPrice = (EditText) findViewById(R.id.price);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDiscount();
            }
        });
        mTakePhoto = (ImageButton) findViewById(R.id.take_photo);
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePhoto();
            }
        });
    }

    private void initCategory() {
        mCategory = (Spinner) findViewById(R.id.category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategory.setAdapter(adapter);
    }

    private void createDiscount() {
        try {
            JSONObject discountJsonObject = new JSONObject();
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            discountJsonObject.put("user_id", userJsonObject.getInt("id"));
            discountJsonObject.put("title", mTitle.getText().toString());
            discountJsonObject.put("description", mDescription.getText().toString());
            discountJsonObject.put("price", mPrice.getText().toString());
            discountJsonObject.put("category", mCategory.getSelectedItem().toString());
            discountJsonObject.put("location_attributes", mCurrentLocation.toJSONObject());
            if(mPhotoData != null) {
                discountJsonObject.put("photo_attributes", photoAttributes());
            }
            DiscountEndpoint discountEndpoint = new DiscountEndpoint();
            discountEndpoint.post(discountJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            finish();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showError();
                        }
                    }
            );
        }catch (JSONException ex) {
            showError();
        }
    }

    private void showError() {
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.error_create_discount, Toast.LENGTH_LONG);
        toast.show();
    }

    private JSONObject photoAttributes() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("data", mPhotoData);
            jsonObject.put("file_type", "jpg");
        }catch (JSONException ex) {}
        return jsonObject;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Camera.CAPTURE_IMAGE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data"); ;
                mTakePhoto.setBackground(new BitmapDrawable(getResources(),bitmap));
                mPhotoData = Camera.encodeToBase64(bitmap);
            }
        }
    }
}
