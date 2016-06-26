package rs.elfak.mosis.marko.discounthunt.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by marko on 6/26/16.
 */
public class Location {
    private double lat, lng;

    public Location(JSONObject jsonObject) {
        try {
            this.lat = jsonObject.getDouble("lat");
            this.lng = jsonObject.getDouble("lng");
        }catch (JSONException ex) {

        }
    }
}
