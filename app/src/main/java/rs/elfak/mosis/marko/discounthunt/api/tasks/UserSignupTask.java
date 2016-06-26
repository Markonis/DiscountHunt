package rs.elfak.mosis.marko.discounthunt.api.tasks;

import android.os.AsyncTask;

public class UserSignupTask extends AsyncTask<Void, Void, Boolean> {
    private final String mEmail;
    private final String mPassword;
    private boolean success;

    public UserSignupTask(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: register the new account here.
        return true;
    }
}