package rs.elfak.mosis.marko.discounthunt;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class DiscountHunt extends Application {
    public static Context context;
    public static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        requestQueue = Volley.newRequestQueue(context);
    }
}
