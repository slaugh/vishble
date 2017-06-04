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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stampy on 15/6/11.
 */
public class SkaleHelperOverriden2 implements SkaleKit.SkaleListener, SkaleKit.WeightListener, SkaleFinder.OnDiscoveryListener{
//    private static final String TAG = "SkaleUtils";

    public interface Listener{
        void onButtonClicked(int id);
        void onWeightUpdate(float weight);
        void onBindRequest();
        void onBond();
        void onConnectResult(boolean success);
        void onDisconnected();
        void onBatteryLevelUpdate(int level);
        void onDeviceScanned(BTDeviceFinder.BluetoothDeviceInfo deviceInfo);
    }

    private enum State {
        WORK,
        PAUSE,
        DESTROY
    }


    private Activity mActivity;
//    private Context mContext;

    private SkaleKit mSkale;
    private SkaleFinder mSkaleFinder;

    private Timer mConnectTimer;

    private com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.Listener mListener;

    private com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.State mState;
    private boolean mIsConnecting;

    private Handler mHandler;

    private String mTargetAddress;

    public SkaleHelperOverriden2(Activity activity){

//        Log.d(TAG, "SkaleHelper, create new instance");

        mActivity = activity;
//        mContext = c;
        mSkaleFinder = new SkaleFinder(activity);
        mIsConnecting = false;

        mState = com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.State.WORK;

        mHandler = new Handler();
    }

    public boolean isBluetoothEnable(){
        return mSkaleFinder.isBluetoothEnable();
    }

    public boolean isConnected(){
        if(mSkale==null){
            return false;
        }else{
            return mSkale.isConnected();
        }
    }

    public void destroy(){
//        Log.d(TAG, "SkaleHelper, destroy");

        mSkaleFinder.close();
        mState = com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.State.DESTROY;
    }

    public void pause(){
//        Log.d(TAG, "SkaleHelper, pause");

        mState = com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.State.PAUSE;
        mSkaleFinder.cancelDiscovery();

        if(mSkale!=null){
            mSkale.setSkaleListener(null);
            mSkale.setWeightListener(null);
            mSkale.disconnect();
            mSkale = null;

//            Log.d(TAG, "SkaleHelper, disconnected");
        }
        mIsConnecting = false;
    }

    public void resume(){
//        Log.d(TAG, "SkaleHelper, resume");

        mState = com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.State.WORK;
        if(mSkale==null){
            findAndConnectToSkale();
        }
    }

    public void setListener(com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.Listener l){
        mListener = l;
    }

    public String getDeviceAddress(){
        if(mSkale!=null){
            return mSkale.getAddress();
        }else{
            return null;
        }
    }

//    public void setTargetDeviceAddress(String address){
//        mTargetAddress = address;
//    }

    public void requestBatteryLevel(){
        if(mSkale!=null){
            mSkale.requestBatteryLevel();
        }
    }

    public void tare(){
        if(mSkale!=null){
            mSkale.tare();
        }
    }

    public void findAndConnectToSkale(){

//        Log.d(TAG, "skaleHelper, findAndConnectToSkale, mIsConnecting = " +  mIsConnecting);

        if(mIsConnecting){
            return;
        }

        mSkaleFinder.setOnDiscoveryListener(this);
        mSkaleFinder.startDiscovery();

        mIsConnecting = true;
    }

    public void disconnect(){
        if(mSkale!=null){
            mSkale.setSkaleListener(null);
            mSkale.setWeightListener(null);
            mSkale.disconnect();
            mSkale = null;
        }
    }

    @Override
    public void onConnected() {
//        Log.d("SkaleHelperLog", "SkaleHelper, onConnected");

        if(mConnectTimer!=null){
            mConnectTimer.cancel();
            mConnectTimer = null;
        }

        mSkale.setWeightListener(this);
        mSkale.setSkaleListener(this);

        mSkale.startMeasure();
        mSkale.startListenButton();

        mSkale.requestBatteryLevel();
        mIsConnecting = false;

        if(mListener!=null){
            mListener.onConnectResult(true);
        }
    }

    @Override
    public void onDisconnected() {

        mSkale.setSkaleListener(null);
        mSkale.setWeightListener(null);
        mSkale = null;

        mIsConnecting = false;

        if(mListener!=null){
            mListener.onDisconnected();
        }
    }

    @Override
    public void onButtonClicked(int id) {
        if(mListener!=null){
            mListener.onButtonClicked(id);
        }
    }

    @Override
    public void onErrorOccur(SkaleKit.ERROR error) {

    }

    @Override
    public void onRssiUpdate(int rssi) {

    }

    @Override
    public void onBatteryLevelUpdate(int batteryLevel) {
        if(mListener!=null){
            mListener.onBatteryLevelUpdate(batteryLevel);
        }
    }

    @Override
    public void onWeightUpdate(float weight) {

        if(mListener!=null){
            mListener.onWeightUpdate(weight);
        }
    }

    /*
     * SkaleFinder.OnDiscoverListener
     */
    @Override
    public void onDiscoveryStart() {
    }

    @Override
    public void onDeviceScanned(BTDeviceFinder.BluetoothDeviceInfo deviceInfo) {
        BluetoothDevice device = deviceInfo.device;

        //TODO: Refactor this code... in fact we want to just save the deviceInfo to a list that we can connect to at a later time.
        Log.w("SKALE OVERRIDE", "Device Name: " + device.getName());

        //I think we can overlook this for now since it does not seem like anyone is setting mTargetAddress anywhere
//        boolean isTargetFind = false; //Scott Notes: This is a local variable... no problem
//        if(mTargetAddress==null){  //Scott Notes: This could be a problem because it can be set from an external interface
//            isTargetFind = true;
//        }else{
//            String address = device.getAddress();
//            if(address!=null && address.equalsIgnoreCase(mTargetAddress)){
//                isTargetFind = true;
//            }
//        }
//
//        if(!isTargetFind){
//            return;
//        }


        //TODO: 1. I think we need to create public accessors for the mSkaleFinder so that we cann access it from the UI thread
        //Also we want to cancel discovery only when we select a device to use
//        mSkaleFinder.setOnDiscoveryListener(null);
//        mSkaleFinder.cancelDiscovery();

        //TODO: 2. Figure out what to do here -> We can probably just leave it
        if(mActivity.isDestroyed()){
            return;
        }

        //TODO: 3. Figure out what to do here -> I think we need to create an interface to allow a connection when device is passed
//        if(device.getBondState()==BluetoothDevice.BOND_BONDED){
//            performConnect(device);
//        }else{
//            performBind(device);
//        }

        //This is the key bit here that allows the function to be overriden in our app
        if(mListener!=null){
            mListener.onDeviceScanned(deviceInfo);
        }
    }

    public void cancelDiscovery(){
        mSkaleFinder.setOnDiscoveryListener(null);
        mSkaleFinder.cancelDiscovery();
    }

    public void connectToDevice(BTDeviceFinder.BluetoothDeviceInfo deviceInfo){
        BluetoothDevice device = deviceInfo.device;
        if(device.getBondState()==BluetoothDevice.BOND_BONDED){
            performConnect(device);
        }else{
            performBind(device);
        }
    }

    @Override
    public void onDiscoveryFinish() {

        mIsConnecting = false;

        if(mSkale==null){
            findAndConnectToSkale();
        }else if(!mSkale.isConnected()){

            mSkale.setSkaleListener(null);
            mSkale.setWeightListener(null);
            mSkale = null;

            findAndConnectToSkale();
        }
    }


    private class BindBroadcastReceiver extends BroadcastReceiver {
        private BluetoothDevice mBtDevice;

        public BindBroadcastReceiver(BluetoothDevice device){
            mBtDevice = device;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

            // skip other devices
            if (!device.getAddress().equals(mBtDevice.getAddress()))
                return;

            if (bondState == BluetoothDevice.BOND_BONDED) {
                context.unregisterReceiver(this);
                performConnect(mBtDevice);

                if(mListener!=null){
                    mListener.onBond();
                }
            }
//            else{
//                if(mListener!=null){
//                    mListener.onBond(false);
//                }
//            }
        }
    }

    @SuppressLint("NewApi")
    private void performBind(BluetoothDevice device){
        if(android.os.Build.VERSION.SDK_INT>=19){

            com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.BindBroadcastReceiver receiver = new com.vish.vish.vishbluetoothtest.SkaleHelperOverriden2.BindBroadcastReceiver(device);
            final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(receiver, filter);

            device.createBond();
            if(mListener!=null){
                mListener.onBindRequest();
            }
        }


    }

    protected void performConnect(BluetoothDevice device){

        if(mSkale!=null){
            return;
        }

        if(mConnectTimer!=null){
            mConnectTimer.cancel();
        }

        mSkale = new SkaleKit(mActivity, device);
        mSkale.setSkaleListener(this);


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean refreshSetting = pref.getBoolean("switch_bluetooth_refresh_cache", true);


        Log.d("SkaleHelperOverriden2", "refreshSetting = " + refreshSetting);
        mSkale.connect(refreshSetting);

        mConnectTimer = new Timer(true);
        mConnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                mIsConnecting = false;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "connect time out", Toast.LENGTH_LONG).show();
                        if(mConnectTimer!=null){
                            mConnectTimer.cancel();
                            mConnectTimer = null;
                        }

                        // TODO connect time out
                        if(mSkale==null){
                            if(mListener!=null){
                                mListener.onConnectResult(false);
                            }
                            findAndConnectToSkale();
                        }else{
                            if (!mSkale.isConnected()) {
                                mSkale.setSkaleListener(null);
                                mSkale.disconnect();
                                mSkale = null;
                                if(mListener!=null){
                                    mListener.onConnectResult(false);
                                }
                                findAndConnectToSkale();
                            }
                        }
                    }
                });
            }
        }, 5000);

    }


//    handle permission

    public static boolean hasPermission(Activity activity){
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestBluetoothPermission(Activity activity, int requestCode){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);

        } else {

            // Camera permission has not been granted yet. Request it directly.
            Log.i("SkaleHelperOverriden2", "Displaying camera permission rationale to provide additional context.");
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }
    }

    public static boolean checkPermissionRequest(int requestCode, @NonNull String[] permissions,
                                                 @NonNull int[] grantResults){

        Log.i("SkaleHelperOverriden2", "Received response for location permission request.");


        for(int i=0; i<grantResults.length; i++){
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(mActivity, "no " + permissions[i] + " permission for taking picture", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;

    }

}
