package rs.elfak.mosis.marko.discounthunt.api.endpoints;


import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;


/**
 * Created by marko on 6/26/16.
 */
public class DiscountEndpoint extends BaseEndpoint {
    public DiscountEndpoint() {
        super("/discounts", "discount");
    }
}
