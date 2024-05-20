package com.example.lanya.Utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth {
    private final Activity activity;                                                                // 页面实例
    public BluetoothAdapter mbluetoothAdapter;                                                      // 蓝牙实例
    private BluetoothLeScanner mBluetoothLeScanner;                                                 // 蓝牙搜索实例
    public Set<BluetoothDevice> mBluetoothDevice;                                                   // 蓝牙设备列表(不重复)
    public ScanCallback mScanCallback;                                                              // 全局搜索回调函数对象

    public Bluetooth(Activity activity) {
        this.activity = activity;
    }

    public void init() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,},
                    3);
        } else {
            Log.i("蓝牙权限", "已获取蓝牙全部权限");
        }

        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice = new HashSet<>();

        if (mbluetoothAdapter == null) {
            Log.e("蓝牙功能", "设备不支持蓝牙");
        }

        try {
            if (mbluetoothAdapter.isEnabled()) {
                Log.i("蓝牙功能", "蓝牙功能已开启");
            } else {
                Log.e("蓝牙功能", "蓝牙功能未开启");
                Log.e("蓝牙功能", "请求用户开启中");
                if (mbluetoothAdapter.enable()) {
                    Log.i("蓝牙功能", "蓝牙功能被用户同意开启");
                } else {
                    Log.i("蓝牙功能", "蓝牙功能被用户拒绝开启");
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public ScanCallback mCustomScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result != null && result.getDevice() != null) {
                BluetoothDevice device = result.getDevice();
                if (mBluetoothDevice.add(device)) {
                    try {
                        String deviceName = device.getName();
                        if (deviceName == null || deviceName.isEmpty()) {
                            Log.i("蓝牙设备", "未知设备：已用MAC代替 [" + device.getAddress() + "]");
                        }
                        else {
                            Log.i("蓝牙设备", "设备名：" + deviceName + "\tMAC：[" + device.getAddress() + "]");
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public void open(ScanCallback scanCallback) {
        mScanCallback = scanCallback;
        mBluetoothLeScanner = mbluetoothAdapter.getBluetoothLeScanner();
        try {
            mBluetoothDevice.clear();
            mBluetoothLeScanner.startScan(mScanCallback);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (mBluetoothLeScanner != null && mScanCallback != null) {
                try {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                mScanCallback = null;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver pairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    // 配对成功
                    // 在这里执行一些操作，例如连接到该设备
                    Log.i("蓝牙功能","配对成功");
                    activity.unregisterReceiver(pairingReceiver);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING) {
                    // 配对失败
                    // 在这里执行一些处理，例如显示一个错误消息或者重试配对
                    Log.i("蓝牙功能","配对失败");
                    activity.unregisterReceiver(pairingReceiver);
                }
            }
        }
    };

    public void Pair(String address) {
        try {
            BluetoothDevice device = mbluetoothAdapter.getRemoteDevice(address);
            Log.i("蓝牙功能","开始配对:"+address);
            device.createBond();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            activity.registerReceiver(pairingReceiver, filter);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public Set<BluetoothDevice> getPairList() {
        Set<BluetoothDevice> pairedDevices = null;
        try {
            pairedDevices = mbluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    // 获取设备名称和地址
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    Log.i("蓝牙功能","配对设备名称：" + deviceName + "\t配对设备地址：" + deviceAddress);
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return pairedDevices;
    }

    public static class ConnectedDevice {
        BluetoothDevice mdevice;
        BluetoothGatt mbluetoothGatt;
        Activity mactivity;
        boolean ConStatus = false;

        public ConnectedDevice(BluetoothDevice device, Activity activity) {
            this.mdevice = device;
            this.mactivity = activity;
            try {
                mbluetoothGatt = device.connectGatt(activity, false, connectCallback);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        public ConnectedDevice(BluetoothAdapter bluetoothAdapter, String address, Activity activity) {
            mdevice = bluetoothAdapter.getRemoteDevice(address);
            try {
                mbluetoothGatt = mdevice.connectGatt(activity, false, connectCallback);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        private final BluetoothGattCallback connectCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // 连接成功，发现服务
                    try {
                        mbluetoothGatt.discoverServices();
                        Log.i("蓝牙连接功能","连接成功");
                        ConStatus = true;
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // 断开连接
                    try {
                        mbluetoothGatt.close();
                        Log.i("蓝牙连接功能","连接失败");
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("蓝牙连接功能","正在尝试获取UUID");
                    // 获取已发现的服务
                    List<BluetoothGattService> services = mbluetoothGatt.getServices();
                    for (BluetoothGattService service : services) {
                        UUID serviceUUID = service.getUuid();
                        System.out.println("Service UUID: " + serviceUUID);
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            UUID characteristicUUID = characteristic.getUuid();
                            System.out.println("Characteristic UUID: " + characteristicUUID);
                        }
                    }
                }
            }
        };

        public static class SocketDevice{
            boolean socketStatus = false;
            BluetoothDevice mdevice;
            BluetoothSocket socket = null;
            OutputStream outputStream;
            InputStream inputStream;
            HandlerThread handlerThread;
            Handler handler;
            UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID是固定的 不可修改 其他UUID请自行参考Android开放的蓝牙协议通信结构

            public SocketDevice(BluetoothDevice device) {
                this.mdevice = device;
            }

            public void connect() {
                try {
                    socket = mdevice.createRfcommSocketToServiceRecord(SPP_UUID);
                    socket.connect();
                    socketStatus = true;
                    handlerThread = new HandlerThread("BluetoothHandlerThread");
                    handlerThread.start();
                    handler = new Handler(handlerThread.getLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            read(msg);
                        }
                    };
                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        socketStatus = false;
                    } catch (SecurityException | IOException e2) {
                        e2.printStackTrace();
                        socketStatus = false;
                    }
                }
            }

            public void close() {
                handlerThread.quit();
                try {
                    socketStatus = false;
                    socket.close();
                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

            public boolean send(String msg) {
                if (!socketStatus) {
                    return false;
                }
                try {
                    outputStream = socket.getOutputStream();
                    outputStream.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            public void read(Message msg) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytes;
                    while ((bytes = inputStream.read(buffer)) != -1) {
                        String receivedMessage = new String(buffer, 0, bytes);

                        // 事件处理模型，还未写
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
