package rs.elfak.mosis.marko.discounthunt.api.endpoints;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;
import rs.elfak.mosis.marko.discounthunt.DiscountHunt;

public class BaseEndpoint {

    public static final String API_URL = "";

    private String url;
    public BaseEndpoint(String path) {
        this.url = API_URL + path;
    }

    public void get(int id, Response.Listener successListener,
                    Response.ErrorListener errorListener) {
        execute(url + "/" + id, Request.Method.GET, null, successListener, errorListener);
    }

    public void post(JSONObject requestBody, Response.Listener successListener,
                    Response.ErrorListener errorListener) {
        execute(url, Request.Method.POST, requestBody, successListener, errorListener);
    }

    public void put(int id, JSONObject requestBody, Response.Listener successListener,
                    Response.ErrorListener errorListener) {
        execute(url + "/" + id, Request.Method.PUT, requestBody, successListener, errorListener);
    }

    public void delete(int id, JSONObject requestBody, Response.Listener successListener,
                    Response.ErrorListener errorListener) {
        execute(url + "/" + id,Request.Method.DELETE, requestBody, successListener, errorListener);
    }

    private void execute(String url, int type, JSONObject requestBody, Response.Listener successListener,
                         Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(type,
                url, requestBody, successListener, errorListener);

        DiscountHunt.requestQueue.add(request);
    }
}
