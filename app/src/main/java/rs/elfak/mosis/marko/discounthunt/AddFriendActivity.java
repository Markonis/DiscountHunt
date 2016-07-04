package rs.elfak.mosis.marko.discounthunt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rs.elfak.mosis.marko.discounthunt.api.endpoints.FriendshipEndpoint;
import rs.elfak.mosis.marko.discounthunt.api.endpoints.UserSearchEndpoint;

public class AddFriendActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERABLE = 2;
    private static final int DISCOVERABLE_DURATION = 300;

    private ArrayList<String> mUserNames;
    private ArrayList<JSONObject> mUsers;
    private ArrayList<BluetoothDevice> mUserDevices;
    private ArrayAdapter<String> mListAdapter;
    private ListView mList;

    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mReceiver;
    private BluetoothDevice mCurrentDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        initList();
        initIntentReceiver();
        initBlueTooth();
    }

    private void initList() {
        mUserDevices = new ArrayList<>();
        mUserNames = new ArrayList<>();
        mUsers = new ArrayList<>();

        mList = (ListView) findViewById(R.id.list);
        mListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mUserNames);
        mList.setAdapter(mListAdapter);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = mUserDevices.get(position);
                createBond(device);
            }
        });
    }

    private void initIntentReceiver() {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    deviceFound(device);
                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (state == BluetoothDevice.BOND_BONDED) {
                        deviceBonded(device);
                    }
                }
            }
        };

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
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
            requestDiscoverable();
        }
    }

    private void requestDiscoverable() {
        // Start being discoverable over BT
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
    }

    private void deviceFound(final BluetoothDevice device) {
        final String address = device.getAddress();
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
                                addToList(userJsonObject, device);
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

    private void addToList(JSONObject userJsonObject, BluetoothDevice device) {
        try {
            String fullName = userJsonObject.get("first_name") + " " + userJsonObject.getString("last_name");
            mListAdapter.add(fullName);
            mUserDevices.add(device);
            mUsers.add(userJsonObject);
        } catch (JSONException ex) {
        }
    }

    private void createBond(BluetoothDevice device) {
        mCurrentDevice = device;
        device.createBond();
    }

    private void deviceBonded(BluetoothDevice device) {
        for (int i = 0; i < mUserDevices.size(); i++) {
            if (mUserDevices.get(i).getAddress().equals(device.getAddress())) {
                createFriendship(mUsers.get(i));
                break;
            }
        }
    }

    private void createFriendship(JSONObject userJsonObject) {
        JSONObject friendshipJsonObject = new JSONObject();
        try {
            JSONObject currentUserJsonObject = DiscountHunt.currentSession.getJSONObject("user");
            friendshipJsonObject.put("user_a_id", userJsonObject.getInt("id"));
            friendshipJsonObject.put("user_b_id", currentUserJsonObject.getInt("id"));
            FriendshipEndpoint endpoint = new FriendshipEndpoint();
            endpoint.post(friendshipJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("Friendship created!");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                requestDiscoverable();
            } else {
                finish();
            }
        } else if (requestCode == REQUEST_DISCOVERABLE) {
            if (resultCode == DISCOVERABLE_DURATION) {
                if (!mBluetoothAdapter.startDiscovery()) {
                    finish();
                }
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

    }
}
