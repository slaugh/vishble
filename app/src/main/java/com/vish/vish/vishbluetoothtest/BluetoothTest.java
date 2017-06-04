package com.vish.vish.vishbluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//TODO: Follow up to the meeting with the Vish guys:
// 1. Build a solid Bluetooth code in Java
// 2. Determine if it is possible / easy to build a list with the Skale SDK
// 3. Port the code indo a JAR as soon as possible

import com.atomax.android.skaleutils.Device.BTDeviceFinder;

import java.util.ArrayList;
import java.util.List;
//import com.atomax.android.skaleutils.SkaleHelper;

public class BluetoothTest extends AppCompatActivity implements SkaleHelperOverriden2.Listener, BluetoothTestCustomInterface {

    private static final int REQUEST_BT_ENABLE = 2;
    private static final int REQUEST_BT_PERMISSION = 1;

    private static boolean debug_mode = true;

    private SkaleHelperOverriden2 skaleHelperOverriden2;

    private TextView mStatusTextView;
    private TextView mWeightTextView;
    private TextView mBatteryTextView;

    ArrayList<String> bluetoothDeviceArray = new ArrayList<String>();
    ArrayList<BTDeviceFinder.BluetoothDeviceInfo> deviceInfoArray = new ArrayList<BTDeviceFinder.BluetoothDeviceInfo>();
    ArrayAdapter<String> arrayAdapter;

    //onCreate() function
    //This function will be called when the app starts and only called again when the app is killed and opened again
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skale);

        if(debug_mode==true) {
            Log.w("SKALE TEST", "onCreate()");
        }

        skaleHelperOverriden2 = new SkaleHelperOverriden2(this);
        skaleHelperOverriden2.setListener(this);
        mStatusTextView = (TextView) findViewById(R.id.text_status);
        mWeightTextView = (TextView) findViewById(R.id.text_weight);
        mBatteryTextView = (TextView) findViewById(R.id.text_battery);

        final ListView bluetoothListView = (ListView)findViewById(R.id.bluetooth_list);
        // Create the adapter to convert the array to views
        //    ListAdapter adapter = new ListAdapter(this, bluetoothDeviceArray);
        // Attach the adapter to a ListView
        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, bluetoothDeviceArray);
        bluetoothListView.setAdapter(arrayAdapter);
        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                Log.w("SKALE TEST", "Device #" + position + " was clicked");
                skaleHelperOverriden2.cancelDiscovery();
                skaleHelperOverriden2.connectToDevice(deviceInfoArray.get(position));
            }
        });

        //Bluetooth Lifecycle Personal Reminder
        // 1. Scans for devices
        // 2. When devices are found it lists them
        // 3. When the user selects a device, then it connects to it

        //Skale App:
        // Seems to connect automatically (to the first Skale it finds??? Or does it try to connect to anything?)
        // Questions: What can we hijack in the Skale API???
        // Discovery: Look into the SkaleHelper.Listener implementation -> Looks like I need to override onDeviceScanned()
        //         -> However, it looks like we cannot override it because it is not in the public interface...

        //The Big Innovation is that we created a wrapper that goes around the code that will hug around SkaleHelper -> ScaleHelperOverriden

        //This creates a little floating button that can be used to tare the device from the app
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(debug_mode==true) {
                    skaleHelperOverriden2.tare(); //TODO: We should create a "spinning wheel" here to indicate that the user will need to wait for a bit while it performs Tare
                    Log.w("SKALE TEST", "Floating Button Pressed");
                }
            }
        });
    }

    //This will be called if the user leaves the app (doesn't kill it) and then returns
    @Override
    protected void onResume() {
        super.onResume();
        if(skaleHelperOverriden2.isBluetoothEnable()){
            boolean hasPermission = SkaleHelperOverriden2.hasPermission(this);
            if(hasPermission){
                if(debug_mode==true) {
                    Log.w("SKALE TEST", "Has Bluetooth Permissions");
                }

                skaleHelperOverriden2.resume();
                mStatusTextView.setText("finding skale...");
            }else{
                if(debug_mode==true) {
                    Log.w("SKALE TEST", "Dooes not have Bluetooth Permissions");
                }
                SkaleHelperOverriden2.requestBluetoothPermission(this, REQUEST_BT_PERMISSION);
            }
        }else{
            if(debug_mode==true) {
                Log.w("SKALE TEST", "isBluetoothEnable Failed... enabling");
            }
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, REQUEST_BT_ENABLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        skaleHelperOverriden2.pause();
    }


    //Here is where ASYNC events are handled for the Skale SDK
    @Override
    public void onButtonClicked(int id) {
        Toast.makeText(this, "button " + id + " is clicked", Toast.LENGTH_SHORT).show();
        if(id == 1){
            skaleHelperOverriden2.tare();
        }else{
            //You could add functionality for the square button here
        }
    }

    @Override
    public void onDeviceScanned(BTDeviceFinder.BluetoothDeviceInfo deviceInfo){
        if(debug_mode==true) {
            Log.w("SKALE TEST", deviceInfo.device.getAddress() + " was discovered!");
        }

        if(!bluetoothDeviceArray.contains(deviceInfo.device.getName()+":"+deviceInfo.device.getAddress())){
            deviceInfoArray.add(deviceInfo);
            bluetoothDeviceArray.add(deviceInfo.device.getName()+":"+deviceInfo.device.getAddress());
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onWeightUpdate(float weight) {
        if(debug_mode==true) {
            //This happens way too often to justify all the logging
//            Log.w("SKALE TEST", "onWeightUpdate");
        }
        mWeightTextView.setText(String.format("%1.1f g", weight));
    }

    //Scott Notes: What is this BindRequest()???
    @Override
    public void onBindRequest() {
        if(debug_mode==true) {
            Log.w("SKALE TEST", "onBindRequest()");
        }
        mStatusTextView.setText("New skale found, paring with it.");
    }

    //Scott Notes: What is onBond()???
    @Override
    public void onBond() {
        if(debug_mode==true) {
            Log.w("SKALE TEST", "onBond()");
        }
        mStatusTextView.setText("Paring done, Connecting");
    }

    @Override
    public void onConnectResult(boolean success) {
        if(success){
            if(debug_mode==true) {
                Log.w("SKALE TEST", "onConnectRequest() Success!");
            }
            mStatusTextView.setText("connected");
        } else {
            if(debug_mode==true) {
                Log.w("SKALE TEST", "onConnectRequest() Fail! :'(");
            }
            mStatusTextView.setText("Failed");
        }
    }

    @Override
    public void onDisconnected() {
        if(debug_mode==true) {
            Log.w("SKALE TEST", "onDisconnect()");
        }
        mStatusTextView.setText("disconnected");
    }

    @Override
    public void onBatteryLevelUpdate(int level) {
        if(debug_mode==true) {
            Log.w("SKALE TEST", "onBatteryLevelUpdate! :'(");
        }
        mBatteryTextView.setText(String.format("battery: %02d", level));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_BT_PERMISSION) {
            boolean result = SkaleHelperOverriden2.checkPermissionRequest(requestCode, permissions, grantResults);
            if(result){
                if(debug_mode==true) {
                    Log.w("SKALE TEST", "Result Success! Resuming the skaleHelperOverriden2");
                }
                skaleHelperOverriden2.resume();
            }else{
                if(debug_mode==true) {
                    Log.w("SKALE TEST", "No bluetooth permission!");
                }
                Toast.makeText(this, "No bluetooth permission", Toast.LENGTH_SHORT).show();
            }
            // END_INCLUDE(permission_result)
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}