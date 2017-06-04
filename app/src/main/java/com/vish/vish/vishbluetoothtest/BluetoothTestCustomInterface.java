package com.vish.vish.vishbluetoothtest;

import com.atomax.android.skaleutils.Device.BTDeviceFinder;

/**
 * Created by scott on 2017-06-03.
 */

interface BluetoothTestCustomInterface {
    void onDeviceScanned(BTDeviceFinder.BluetoothDeviceInfo deviceInfo);
}
