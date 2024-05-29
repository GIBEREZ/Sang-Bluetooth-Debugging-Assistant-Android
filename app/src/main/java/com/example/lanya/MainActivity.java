package com.example.lanya;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.lanya.Utils.Bluetooth;
import com.example.lanya.Utils.GPS;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public GPS gps_utils = new GPS(this);
    public Bluetooth bluetooth = new Bluetooth(this);
    private final JcFragment mJcFragment = new JcFragment(bluetooth);
    private final MessageFragment mMessageFragment = new MessageFragment();
    private final GlobalFragment mGlobalFragment = new GlobalFragment(bluetooth);
    private final SettingFragment mSettingFragment = new SettingFragment();
    Bluetooth.ConnectedDevice.SPP_Device Sdevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment_init();

        gps_utils.init();
        bluetooth.init();
        //Bluetooth.ConnectedDevice Cdevice = new Bluetooth.ConnectedDevice(bluetooth.mbluetoothAdapter,"C4:AC:AA:59:86:42", this);
        //Sdevice = new Bluetooth.ConnectedDevice.SPP_Device(Cdevice.mdevice,UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
        //Sdevice.connect();
    }

    /**
     * 多页面+底部导航栏初始化
     */
    @SuppressLint("NonConstantResourceId")
    private void Fragment_init() {
        // 启动APP默认显示监测页面
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.content, mJcFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.content, mMessageFragment, "2").hide(mMessageFragment).commit();
        fragmentManager.beginTransaction().add(R.id.content, mGlobalFragment, "3").hide(mGlobalFragment).commit();
        fragmentManager.beginTransaction().add(R.id.content, mSettingFragment, "4").hide(mSettingFragment).commit();
        // 点击事件设置
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_jc:
                    fragmentManager.beginTransaction().show(mJcFragment).commit();
                    fragmentManager.beginTransaction().hide(mMessageFragment).commit();
                    fragmentManager.beginTransaction().hide(mGlobalFragment).commit();
                    fragmentManager.beginTransaction().hide(mSettingFragment).commit();
                    break;
                case R.id.navigation_message:
                    fragmentManager.beginTransaction().hide(mJcFragment).commit();
                    fragmentManager.beginTransaction().show(mMessageFragment).commit();
                    fragmentManager.beginTransaction().hide(mGlobalFragment).commit();
                    fragmentManager.beginTransaction().hide(mSettingFragment).commit();
                    break;
                case R.id.navigation_global:
                    fragmentManager.beginTransaction().hide(mJcFragment).commit();
                    fragmentManager.beginTransaction().hide(mMessageFragment).commit();
                    fragmentManager.beginTransaction().show(mGlobalFragment).commit();
                    fragmentManager.beginTransaction().hide(mSettingFragment).commit();
                    break;
                case R.id.navigation_setting:
                    fragmentManager.beginTransaction().hide(mJcFragment).commit();
                    fragmentManager.beginTransaction().hide(mMessageFragment).commit();
                    fragmentManager.beginTransaction().hide(mGlobalFragment).commit();
                    fragmentManager.beginTransaction().show(mSettingFragment).commit();
                    break;
            }
            return true;
        });
    }

    /**
     * 请求权限后的回调函数，用于处理权限请求结果并弹出对应的权限获取对话框。
     * @param requestCode 请求码，用于标识权限请求。
     * @param permissions 请求的权限数组。
     * @param grantResults 对应权限请求结果的数组。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 超类重写
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 前台定位权限
        final int LOCATION_PERMISSION_REQUEST_CODE = 1;
        // 后台定位权限
        final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2;
        // 蓝牙全部权限
        final int BLUETOOTH_CODE = 3;
        // 判断请求码是否符合宏定义
        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 前台定位权限已授予，检查后台定位权限（Android 10及以上）
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                    } else {
                        Log.i("定位权限：", permissions[0] + "已允许");
                    }
                } else {
                    Log.e("定位权限：", permissions[0] + "已拒绝");
                }
                break;

            case BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("定位权限：", permissions[0] + "已允许");
                } else {
                    Log.e("定位权限：", permissions[0] + "已拒绝");
                }
                break;

            case BLUETOOTH_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.i("蓝牙权限", permissions[i] + " 已允许.");
                    } else {
                        Log.e("蓝牙权限", permissions[i] + " 已拒绝.");
                    }
                }
                break;
        }
    }
}