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

    public static final String API_URL = "http://discount-hunt-markomedia.c9users.io";

    private String url;
    private String resourceName;
    public BaseEndpoint(String path, String resourceName) {
        this.resourceName = resourceName;
        this.url = API_URL + path;
    }

    public void get(int id, Response.Listener successListener, Response.ErrorListener errorListener) {
        execute(url + "/" + id + ".json", Request.Method.GET, null, successListener, errorListener);
    }

    public void post(JSONObject jsonObject, Response.Listener successListener,
                    Response.ErrorListener errorListener) {
        JSONObject requestBody = createResourceRequest(jsonObject);
        execute(url + ".json", Request.Method.POST, requestBody, successListener, errorListener);
    }

    public void put(int id, JSONObject jsonObject, Response.Listener successListener,
                    Response.ErrorListener errorListener) {
        JSONObject requestBody = createResourceRequest(jsonObject);
        execute(url + "/" + id + ".json", Request.Method.PUT, requestBody, successListener, errorListener);
    }

    public void delete(int id, Response.Listener successListener,Response.ErrorListener errorListener) {
        execute(url + "/" + id, Request.Method.DELETE, null, successListener, errorListener);
    }

    private void execute(String url, int type, JSONObject requestBody,
                         Response.Listener successListener,Response.ErrorListener errorListener) {
        String finalUrl = url;
        boolean hasSession = DiscountHunt.currentSession != null;
        try {
            // Put auth token into the request
            if(requestBody != null && hasSession){
                requestBody.put("token", DiscountHunt.currentSession.getString("token"));
            }else if(hasSession){
                finalUrl = finalUrl + "?token=" + DiscountHunt.currentSession.getString("token");
            }

            // Create request
            JsonObjectRequest request = new JsonObjectRequest(type, finalUrl, requestBody,
                    successListener, errorListener);
            // Add request to queue
            DiscountHunt.requestQueue.add(request);
        }catch (JSONException ex) {}
    }

    private JSONObject createResourceRequest(JSONObject jsonObject) {
        try {
            JSONObject requestJsonObject = new JSONObject();
            requestJsonObject.put(resourceName, jsonObject);
            return requestJsonObject;
        }catch (JSONException ex) { }
        return null;
    }
}
