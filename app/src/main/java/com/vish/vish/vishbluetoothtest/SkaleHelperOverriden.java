package com.vish.vish.vishbluetoothtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.atomax.android.skaleutils.Device.BTDeviceFinder;
import com.atomax.android.skaleutils.Device.SkaleFinder;
import com.atomax.android.skaleutils.Device.SkaleKit;
import com.atomax.android.skaleutils.SkaleHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stampy on 15/6/11.
 */
public class SkaleHelperOverriden extends SkaleHelper implements SkaleKit.SkaleListener, SkaleKit.WeightListener, SkaleFinder.OnDiscoveryListener{

//    private static final String TAG = "SkaleUtils";

    public interface OverridenListener{
        void onButtonClicked(int id);
        void onWeightUpdate(float weight);
        void onBindRequest();
        void onBond();
        void onConnectResult(boolean success);
        void onDisconnected();
        void onBatteryLevelUpdate(int level);
        void onDeviceScanned(BTDeviceFinder.BluetoothDeviceInfo deviceInfo);
    }

    // I think I need a way to cast the OverridenListener to a Listener
//    private SkaleHelper.Listener CastListener(OverridenListener overridenListener){
//        SkaleHelper.Listener listener;
//
//    }

    //TODO: The big problem is that we need the listener to call the instance of the method and not the abstract method when it is not null.
    // So we need to "undo" all of the super calls so that we can call the listner instead of the actual class

    private OverridenListener mOverridenListener;

    public SkaleHelperOverriden(Activity activity){
        super(activity);

//        Log.d(TAG, "SkaleHelper, create new instance");

//        mActivity = activity;
//        mSkaleFinder = new SkaleFinder(activity);
//        mIsConnecting = false;
//
//        mState = State.WORK;
//
//        mHandler = new Handler();
    }

    public boolean isBluetoothEnable(){
        return super.isBluetoothEnable();
    }

    public boolean isConnected(){
        return super.isConnected();
    }

    public void destroy(){
        super.destroy();
    }

    public void pause(){
        super.pause();
    }

    public void resume(){
        super.resume();
    }

    //Scott Notes: ??? Seems sketchy
    public void setListener(OverridenListener l){
        mOverridenListener = l;
    }

    public String getDeviceAddress(){
       return super.getDeviceAddress();
    }

    public void setTargetDeviceAddress(String address){
        super.setTargetDeviceAddress(address);
    }

    public void requestBatteryLevel(){
        super.requestBatteryLevel();
    }

    public void tare(){
       super.tare();
    }

    public void findAndConnectToSkale(){
        super.findAndConnectToSkale();
    }

    @Override
    public void onConnected() {
        super.onConnected();
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
    }

    @Override
    public void onButtonClicked(int id) {
        super.onButtonClicked(id);
    }

    @Override
    public void onErrorOccur(SkaleKit.ERROR error) {

    }

    @Override
    public void onRssiUpdate(int rssi) {

    }

    @Override
    public void onBatteryLevelUpdate(int batteryLevel) {
        super.onBatteryLevelUpdate(batteryLevel);
    }

    @Override
    public void onWeightUpdate(float weight) {
        super.onWeightUpdate(weight);
    }

    /*
     * SkaleFinder.OnDiscoverListener
     */
    @Override
    public void onDiscoveryStart() {
        super.onDiscoveryStart();
    }

    @Override
    public void onDeviceScanned(BTDeviceFinder.BluetoothDeviceInfo deviceInfo) {
            Log.w("SKALE OVERRIDE", "onDeviceScanned was triggered!");
       super.onDeviceScanned(deviceInfo);
    }

    @Override
    public void onDiscoveryFinish() {
        super.onDiscoveryFinish();
    }

    //Scott Notes: Same as member variables... delete this???
//    private class BindBroadcastReceiver extends BroadcastReceiver {
//        private BluetoothDevice mBtDevice;
//        public BindBroadcastReceiver(BluetoothDevice device){
//            mBtDevice = device;
//        }
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
//            final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
//
//            // skip other devices
//            if (!device.getAddress().equals(mBtDevice.getAddress()))
//                return;
//
//            if (bondState == BluetoothDevice.BOND_BONDED) {
//                context.unregisterReceiver(this);
//                performConnect(mBtDevice);
//
//                if(mListener!=null){
//                    mListener.onBond();
//                }
//            }
//        }
//    }

    //Scott Notes: Annotation Delete this, is this overriding?
//    @SuppressLint("NewApi")
//    private void performBind(BluetoothDevice device){
//
//            if(android.os.Build.VERSION.SDK_INT>=19){
//                BindBroadcastReceiver receiver = new BindBroadcastReceiver(device);
//                final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//                mActivity.registerReceiver(receiver, filter);
//                device.createBond();
//                if(mListener!=null){
//                    mListener.onBindRequest();
//                }
//            }
//    }

    protected void performConnect(BluetoothDevice device){
        super.performConnect(device);
    }

    //Handle permission
    public static boolean hasPermission(Activity activity){
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestBluetoothPermission(Activity activity, int requestCode){
        SkaleHelper.requestBluetoothPermission(activity, requestCode);
    }

    public static boolean checkPermissionRequest(int requestCode, @NonNull String[] permissions,
                                                 @NonNull int[] grantResults){
        return SkaleHelper.checkPermissionRequest(requestCode, permissions,
        grantResults);
    }
}
