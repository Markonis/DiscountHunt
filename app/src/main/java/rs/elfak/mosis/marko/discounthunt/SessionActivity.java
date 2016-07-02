package rs.elfak.mosis.marko.discounthunt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserSessionEndpoint;

public class SessionActivity extends AppCompatActivity {

    protected TextView mUsernameView;
    protected EditText mPasswordView;
    protected View mProgressView;
    protected View mCredentialsForm;

    protected void initViews() {
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mCredentialsForm = findViewById(R.id.credentials_form);
        mProgressView = findViewById(R.id.progress_bar);
    }

    protected boolean isUsernameValid(String username) {
        return username.length() > 4;
    }

    protected boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    protected void createSession(final JSONObject userJsonObject) {
        UserSessionEndpoint userSessionEndpoint = new UserSessionEndpoint();
        userSessionEndpoint.post(userJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject sessionJsonObject) {
                        DiscountHunt.currentSession = sessionJsonObject;
                        startMapActivity();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError();
                        showProgress(false);
                    }
                }
        );
    }

    protected void startMapActivity() {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        startActivity(intent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCredentialsForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mCredentialsForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCredentialsForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCredentialsForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    protected void showError() {
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.error_session, Toast.LENGTH_LONG);
        toast.show();
    }
}
