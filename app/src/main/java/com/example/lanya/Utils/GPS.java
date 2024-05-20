package com.example.lanya.Utils;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class GPS {
    private final Activity activity;
    private boolean locationPermissionGranted = false;

    public GPS(Activity activity) {
        this.activity = activity;
    }

    /**
     * 初始化定位权限
     */
    public void init() {
        requestLocationPermissions();
    }

    /**
     * 请求前台后台定位权限，如果用户拒绝其中一项则暂停使用
     */
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        2);
            } else {
                Log.i("定位权限：", "已获取前台和后台定位权限");
                locationPermissionGranted = true;
            }
        }
    }

    /**
     * 获取到经纬度函数
     * LocationManager参数：GPS_PROVIDER（GPS芯片定位）、NETWORK_PROVIDER（网络定位）、PASSIVE_PROVIDER（过去其他APP更新的经纬度数据）
     */
    public void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("定位权限：", "前台和后台定位权限检查失败");
                return;
            }
            Log.i("定位权限：", "已成功检查前台后台定位权限");
            Log.i("定位权限：", "开始获取经纬度数据");
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e("定位权限：", "获取定位权限失败");
        }
    }

    /**
     * Android异步回调函数，位置更新事件
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.i("位置", "纬度: " + latitude + ", 经度: " + longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };
}