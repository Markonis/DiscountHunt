package rs.elfak.mosis.marko.discounthunt.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by marko on 6/26/16.
 */
public class Photo {
    private String data, fileType;

    public Photo(JSONObject jsonObject) {
        try {
            this.data = jsonObject.getString("data");
            this.fileType = jsonObject.getString("file_type");
        }catch (JSONException ex) {

        }
    }
}
