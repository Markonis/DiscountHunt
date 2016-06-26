package rs.elfak.mosis.marko.discounthunt.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by marko on 6/26/16.
 */
public class Setting {
    private boolean enableBackgroundProcess;
    private int searchRadius;

    public Setting(JSONObject jsonObject) {
        try {
            this.enableBackgroundProcess = jsonObject.getBoolean("enable_background_process");
            this.searchRadius = jsonObject.getInt("search_radius");
        }catch (JSONException ex) {

        }
    }
}
