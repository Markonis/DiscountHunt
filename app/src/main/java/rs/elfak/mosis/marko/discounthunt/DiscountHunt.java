package rs.elfak.mosis.marko.discounthunt;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class DiscountHunt extends Application {
    public static Context context;
    public static RequestQueue requestQueue;
    public static JSONObject currentUser;
    public static JSONObject currentSession;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        requestQueue = Volley.newRequestQueue(context);
    }
}
