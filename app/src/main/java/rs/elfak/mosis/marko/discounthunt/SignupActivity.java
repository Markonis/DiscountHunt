package rs.elfak.mosis.marko.discounthunt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserSessionEndpoint;

/**
 * A login screen that offers login via username/password.
 */
public class SignupActivity extends SessionActivity {

    // UI references.
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mPhoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initViews();

        mFirstNameView = (EditText) findViewById(R.id.signup_first_name);
        mLastNameView = (EditText) findViewById(R.id.signup_last_name);
        mPhoneView = (EditText) findViewById(R.id.signup_phone);

        Button signupButton = (Button) findViewById(R.id.signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String phone = mPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            try {
                JSONObject userJsonObject = new JSONObject();
                userJsonObject.put("username", username);
                userJsonObject.put("password", password);
                userJsonObject.put("first_name", firstName);
                userJsonObject.put("last_name", lastName);
                userJsonObject.put("phone", phone);
                userJsonObject.put("user_devices_attributes", userDevicesJsonArray());
                createUser(userJsonObject);
            } catch (Exception ex) {
                showError();
                finish();
            }
        }
    }

    private String getBlueToothMAC() throws Exception {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter != null) {
            return mAdapter.getAddress();
        } else {
            throw new Exception("BlueTooth is not supported!");
        }
    }

    private JSONArray userDevicesJsonArray() throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject deviceJsonObject = new JSONObject();
        deviceJsonObject.put("hardware_uuid", getBlueToothMAC());
        jsonArray.put(deviceJsonObject);
        return jsonArray;
    }

    private void createUser(final JSONObject userJsonObject) {
        UserEndpoint userEndpoint = new UserEndpoint();
        userEndpoint.post(userJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        createSession(userJsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false);
                        showError();
                    }
                }
        );
    }
}

