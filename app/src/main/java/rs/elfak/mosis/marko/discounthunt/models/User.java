package rs.elfak.mosis.marko.discounthunt.models;

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
}
