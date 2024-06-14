package com.example.lanya;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.lanya.Utils.Bluetooth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class commActivity extends Activity {
    LinearLayout communication_write_Linear;
    LinearLayout communication_read_Linear;
    BluetoothGattCharacteristic mCharacteristic;
    BluetoothGatt mBluetoothGatt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.communication_layout);

        UI_init();

        EventBus.getDefault().register(this);
        MessageFragment.CharacteristicEvent stickyEvent = EventBus.getDefault().getStickyEvent(MessageFragment.CharacteristicEvent.class);
        if (stickyEvent != null) {
            onCharacteristicEvent(stickyEvent);
            EventBus.getDefault().removeStickyEvent(stickyEvent);
        }
    }

    public void UI_init() {
        communication_read_Linear = findViewById(R.id.communication_read_Linear);
        communication_write_Linear = findViewById(R.id.communication_write_Linear);

        EditText communication_read_input = findViewById(R.id.communication_read_input);
        EditText communication_write_input = findViewById(R.id.communication_write_input);

        TextView communication_read_button = findViewById(R.id.communication_read_button);
        TextView communication_write_button = findViewById(R.id.communication_write_button);

        communication_read_button.setOnClickListener(v -> {
            Log.i("蓝牙传输功能","用户请求向指定特征进行读取");
            try {
                mBluetoothGatt.readCharacteristic(mCharacteristic);
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e("蓝牙传输功能","读取异常");
            }
            Log.i("蓝牙传输功能","读取完毕");
        });

        communication_write_button.setOnClickListener(v -> {
            Log.i("蓝牙传输功能","用户向指定特征写入Value");
            String value = communication_write_input.getText().toString();
            if (value.equals("")) {
                Log.e("蓝牙传输功能","写入值为空！");
                return;
            }
            mCharacteristic.setValue(value.getBytes());
            try {
                mBluetoothGatt.writeCharacteristic(mCharacteristic);
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e("蓝牙传输功能","写入异常");
            }
            Log.i("蓝牙传输功能","写入完毕");
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCharacteristicEvent(MessageFragment.CharacteristicEvent event) {
        mCharacteristic = event.mCharacteristic;
        mBluetoothGatt = event.mBluetoothGatt;

        Log.i("UI界面","已初始化完毕commActivity，接下来接收参数，生成UI界面的数据");

        String PCOTOCOL = Bluetooth.getCharacteristicProperties(mCharacteristic);
        if (PCOTOCOL.contains("PROPERTY_WRITE")) {
            communication_write_Linear.setVisibility(View.VISIBLE);
            findViewById(R.id.WRITE).setVisibility(View.VISIBLE);
        }
        if (PCOTOCOL.contains("PROPERTY_READ")) {
            communication_read_Linear.setVisibility(View.VISIBLE);
            findViewById(R.id.READ).setVisibility(View.VISIBLE);
        }
        if (PCOTOCOL.contains("PROPERTY_WRITE_NO_RESPONSE")) {
            communication_write_Linear.setVisibility(View.VISIBLE);
            findViewById(R.id.WRITE_NO_RESPONSE).setVisibility(View.VISIBLE);
        }
        if (PCOTOCOL.contains("PROPERTY_NOTIFY")) {
            findViewById(R.id.NOTIFY).setVisibility(View.VISIBLE);
            findViewById(R.id.NOTIFY_button).setVisibility(View.VISIBLE);
        }
        if (PCOTOCOL.contains("PROPERTY_INDICATE")) {
            findViewById(R.id.INDICATE).setVisibility(View.VISIBLE);
        }
        if (PCOTOCOL.contains("PROPERTY_BROADCAST")) {
            findViewById(R.id.BROADCAST).setVisibility(View.VISIBLE);
        }
        if (PCOTOCOL.contains("PROPERTY_SIGNED_WRITE")) {
            findViewById(R.id.SIGNED_WRITE).setVisibility(View.VISIBLE);
        }
        if (PCOTOCOL.contains("PROPERTY_EXTENDED_PROPS")) {
            findViewById(R.id.EXTENDED_PROPS).setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onfeatureDataEvent(Bluetooth.featureData event) {
        if (event.mBluetoothGatt == mBluetoothGatt && event.mCharacteristic == mCharacteristic) {
            EditText communication_read_input = findViewById(R.id.communication_read_input);
            communication_read_input.setText(event.mValue.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("UI界面","检测到用户返回键，销毁当前Activity");
        finish();
    }
}
