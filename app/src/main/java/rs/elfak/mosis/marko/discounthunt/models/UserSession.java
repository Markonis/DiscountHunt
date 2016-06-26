package rs.elfak.mosis.marko.discounthunt.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by marko on 6/26/16.
 */
public class UserSession {
    private String token;

    public UserSession(JSONObject jsonObject) {
        try {
            this.token = jsonObject.getString("token");
        }catch (JSONException ex) {

        }
    }
}
