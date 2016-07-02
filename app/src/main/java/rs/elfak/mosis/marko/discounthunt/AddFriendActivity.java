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

import java.util.ArrayList;

public class AddFriendActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERABLE = 2;
    private static final int DISCOVERABLE_DURATION = 300;

    private ArrayList<BluetoothDevice> mDevices;
    private ArrayAdapter<BluetoothDevice> mListAdapter;
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
        mDevices = new ArrayList<>();
        mList = (ListView) findViewById(R.id.list);
        mListAdapter = new ArrayAdapter<BluetoothDevice>(getApplicationContext(),
                android.R.layout.simple_list_item_1, mDevices);
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
        }else{
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
                    // Add the name and address to an array adapter to show in a ListView
                    mListAdapter.add(device);
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        if(!mBluetoothAdapter.startDiscovery()){
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                onBlueToothEnabled();
            }else{
                finish();
            }
        }else if(requestCode == REQUEST_DISCOVERABLE) {
            if(resultCode == DISCOVERABLE_DURATION){
                onDiscoverableEnabled();
            }else{
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
