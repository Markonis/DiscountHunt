package rs.elfak.mosis.marko.discounthunt.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by marko on 6/26/16.
 */
public class User {
    private String firstName, lastName, phone, username, password;
    private Photo photo;
    private Location location;
    private ArrayList<User> friends;

    public User(JSONObject jsonObject) {
        try {
            this.firstName = jsonObject.getString("first_name");
            this.lastName = jsonObject.getString("last_name");
            this.phone = jsonObject.getString("phone");
            this.photo = new Photo(jsonObject.getJSONObject("photo"));
            this.location = new Location(jsonObject.getJSONObject("location"));
            if(jsonObject.has("friends")){
                initFriends(jsonObject.getJSONArray("friends"));
            }
        }catch (JSONException ex) {

        }
    }

    private void initFriends(JSONArray jsonArray) throws JSONException {
        this.friends = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            this.friends.add(new User(jsonArray.getJSONObject(i)));
        }
    }
}
