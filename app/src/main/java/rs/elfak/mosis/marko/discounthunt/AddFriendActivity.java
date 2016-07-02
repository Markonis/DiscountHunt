package rs.elfak.mosis.marko.discounthunt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserSearchEndpoint;

public class AddFriendActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERABLE = 2;
    private static final int DISCOVERABLE_DURATION = 300;

    private ArrayList<String> mUsers;
    private ArrayAdapter<String> mListAdapter;
    private ListView mList;

    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        initList();
        initBlueTooth();
    }

    private void initList() {
        mUsers = new ArrayList<>();
        mList = (ListView) findViewById(R.id.list);
        mListAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, mUsers);
        mList.setAdapter(mListAdapter);
    }

    private void initBlueTooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            onBlueToothEnabled();
        }
    }

    private void onBlueToothEnabled() {
        // Start being discoverable over BT
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
    }

    private void onDiscoverableEnabled() {
        // Register intent receiver for BluetoothDevice.ACTION_FOUND
        // to receive an event when a device is found
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    onDeviceFound(device);
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        if (!mBluetoothAdapter.startDiscovery()) {
            finish();
        }
    }

    private void onDeviceFound(BluetoothDevice device) {
        String address = device.getAddress();
        UserSearchEndpoint searchEndpoint = new UserSearchEndpoint();
        JSONObject searchJsonObject = new JSONObject();
        try {
            searchJsonObject.put("hardware_uuid", address);
            searchEndpoint.post(searchJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            JSONObject userJsonObject = getUserJson(response);
                            if (userJsonObject != null) {
                                addUserToList(userJsonObject);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );
        } catch (JSONException ex) {
        }
    }

    private JSONObject getUserJson(JSONObject searchJsonObject) {
        try {
            String resultStr = searchJsonObject.getString("result");
            JSONArray jsonArray = new JSONArray(resultStr);
            return jsonArray.getJSONObject(0);
        } catch (JSONException ex) {
            return null;
        }
    }

    private void addUserToList(JSONObject userJsonObject) {
        try {
            String fullName = userJsonObject.get("first_name") + " " + userJsonObject.getString("last_name");
            mListAdapter.add(fullName);
        } catch (JSONException ex) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                onBlueToothEnabled();
            } else {
                finish();
            }
        } else if (requestCode == REQUEST_DISCOVERABLE) {
            if (resultCode == DISCOVERABLE_DURATION) {
                onDiscoverableEnabled();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
