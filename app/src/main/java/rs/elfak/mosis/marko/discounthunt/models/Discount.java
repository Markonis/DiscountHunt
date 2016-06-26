package rs.elfak.mosis.marko.discounthunt.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by marko on 6/26/16.
 */
public class Discount {
    private String title, description, category;
    private int votes;
    private double price;
    private Photo photo;
    private User user;
    private Location location;

    public Discount(JSONObject jsonObject) {
        try {
            this.title = jsonObject.getString("title");
            this.description = jsonObject.getString("description");
            this.category = jsonObject.getString("category");
            this.votes = jsonObject.getInt("votes");
            this.photo = new Photo(jsonObject.getJSONObject("photo"));
            this.location = new Location(jsonObject.getJSONObject("location"));
            this.user = new User(jsonObject.getJSONObject("user"));
        }catch (JSONException ex) {

        }
    }
}
