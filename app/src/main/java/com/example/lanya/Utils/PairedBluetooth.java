package com.example.lanya.Utils;

import android.bluetooth.BluetoothDevice;;

public class PairedBluetooth {
    private final BluetoothDevice mbluetoothDevice;
    private final String maddress;

    public PairedBluetooth(BluetoothDevice bluetoothDevice) {
        mbluetoothDevice = bluetoothDevice;
        maddress = mbluetoothDevice.getAddress();
    }
}
