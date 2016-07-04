package rs.elfak.mosis.marko.discounthunt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.SettingEndpoint;

public class SettingActivity extends AppCompatActivity {

    private Switch mEnableBackgroundProcess;
    private TextView mSearchRadius;
    private JSONObject mSettingJsonObject;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        try {
            mSettingJsonObject = DiscountHunt.currentSession
                    .getJSONObject("user").getJSONObject("setting");

            mSearchRadius = (TextView) findViewById(R.id.search_radius);
            mEnableBackgroundProcess = (Switch) findViewById(R.id.enable_background_process);
            mSaveButton = (Button) findViewById(R.id.save);
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSetting();
                }
            });

            updateView();
        }catch (JSONException ex) {}
    }

    private void updateView() {
        try {
            mSearchRadius.setText(String.valueOf(mSettingJsonObject.getInt("search_radius")));
            mEnableBackgroundProcess.setChecked(mSettingJsonObject.getBoolean("enable_background_service"));
        }catch (JSONException ex){}
    }

    private void saveSetting() {
        try {
            JSONObject jsonObject = new JSONObject(mSettingJsonObject.toString());
            jsonObject.put("enable_background_process", mEnableBackgroundProcess.isChecked());
            jsonObject.put("search_radius", mSearchRadius.getText().toString());
            int id = jsonObject.getInt("id");
            SettingEndpoint endpoint = new SettingEndpoint();
            endpoint.put(id, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            saved(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            updateView();
                        }
                    });
        }catch (JSONException ex){}
    }

    private void saved(JSONObject response) {
        try {
            mSettingJsonObject = response;
            JSONObject userJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            userJsonObject.put("setting", response);
            DiscountHunt.currentSession.put("user", userJsonObject);
            finish();
        }catch (JSONException ex){}
    }
}
