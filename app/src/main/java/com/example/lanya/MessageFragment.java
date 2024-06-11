package com.example.lanya;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lanya.Utils.Bluetooth;
import com.example.lanya.Utils.PairedBluetooth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageFragment extends Fragment {

    Bluetooth mbluetooth;
    private ViewPager2 viewpager2;
    private CustomPagerAdapter customPagerAdapter;

    public MessageFragment(Bluetooth bluetooth) {
        mbluetooth = bluetooth;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        viewpager2 = view.findViewById(R.id.viewPager);
        customPagerAdapter = new CustomPagerAdapter();
        viewpager2.setAdapter(customPagerAdapter);

        viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 输出当前选中页面的索引
                Log.d("ViewPager", "当前索引: " + position);
            }
        });
        return view;
    }


    /*@Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i("蓝牙连接功能", "正在尝试获取SPP_UUID");
            String deviceUuid = null;
            try {
                deviceUuid = mdevice.getUuids()[0].toString();
                Log.i("蓝牙连接功能", "SPP协议 UUID:"+deviceUuid);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            Log.i("蓝牙连接功能", "正在尝试获取BLE_UUID");
            List<BluetoothGattService> services = mbluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                UUID serviceUUID = service.getUuid();
                Log.i("蓝牙连接功能", "服务 UUID: " + serviceUUID);
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    UUID characteristicUUID = characteristic.getUuid();
                    Log.i("蓝牙连接功能", "特征 UUID: " + characteristicUUID);
                }
            }

            if (services.size() != 0 && deviceUuid != null) {
                type = PCOTOCOL_BLESPP;
                Log.i("蓝牙连接功能", "该设备是 BLE | SPP 双协议");
            }
            else if (services.size() != 0) {
                type = PCOTOCOL_BLE;
                Log.i("蓝牙连接功能", "该设备是BLE协议");
            }
            else if (deviceUuid != null) {
                type = PCOTOCOL_SPP;
                Log.i("蓝牙连接功能", "该设备是SPP协议");
            }
        }
    } */

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConnectionEvent(JcFragment.ConnectionEvent event) {
        final BluetoothGatt[] bluetoothGatt = new BluetoothGatt[1];
        new Bluetooth.ConnectedDevice(mbluetooth.mbluetoothAdapter, event.mMac, getActivity(), new Bluetooth.ConnectedDevice.ConnectionCallback() {
            @Override
            public void onConnectSuccess(BluetoothGatt mBluetoothGatt, String PCOTOCOL) {
                bluetoothGatt[0] = mBluetoothGatt;
                customPagerAdapter.addItem(event.mDevice, PCOTOCOL, bluetoothGatt[0]);
            }

            @Override
            public void onConnectFailed(BluetoothDevice device) {
                customPagerAdapter.deleteItem(device);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public class CustomPagerAdapter extends RecyclerView.Adapter<CustomPagerAdapter.CustomViewHolder> {

        private List<BluetoothDevice> mDeviceList;
        private List<String> PCOTOCOL_List;
        private List<BluetoothGatt> mGattList;

        private int MaxNum = 8;

        public CustomPagerAdapter() {
            this.mDeviceList = new ArrayList<>();
            this.PCOTOCOL_List = new ArrayList<>();
            this.mGattList = new ArrayList<>();
        }

        public void addItem(BluetoothDevice device, String PCOTOCOL,BluetoothGatt bluetoothGatt) {
            if (MaxNum == mDeviceList.size())
            {
                Toast.makeText(getContext(), "已经添加8个，不能再添加了", Toast.LENGTH_SHORT).show();
                return;
            }
            int index = mDeviceList.indexOf(device);
            if (index != -1) {
                mDeviceList.remove(index);
                PCOTOCOL_List.remove(index);
                mGattList.remove(index);
                notifyItemRemoved(index);
            }
            mDeviceList.add(device);
            PCOTOCOL_List.add(PCOTOCOL);
            mGattList.add(bluetoothGatt);
            notifyItemInserted(mDeviceList.size() - 1);
            Log.i("UI界面","MessageFragment新的蓝牙连接已添加进UI界面");
        }

        public void deleteItem(BluetoothDevice device) {
            int index = mDeviceList.indexOf(device);
            if (index != -1) {
                mDeviceList.remove(index);
                PCOTOCOL_List.remove(index);
                mGattList.remove(index);
                notifyItemRemoved(index);
                Log.i("UI界面","MessageFragment曾经蓝牙连接已在UI界面删除");
            }
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            BluetoothDevice device = mDeviceList.get(position);
            String PCOTOCOL = PCOTOCOL_List.get(position);
            BluetoothGatt gatt = mGattList.get(position);
            try {
                holder.viewpager_name.setText("设备名称:  " + device.getName());
                holder.viewpager_protocol.setText("设备协议:  " + PCOTOCOL);
                holder.viewpager_uuid.setText("设备地址:  " + device.getAddress());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }



        @Override
        public int getItemCount() {
            return mDeviceList.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView viewpager_name;
            TextView viewpager_protocol;
            TextView viewpager_uuid;

            CustomViewHolder(View itemView) {
                super(itemView);
                viewpager_name = itemView.findViewById(R.id.viewpager_name);
                viewpager_protocol = itemView.findViewById(R.id.viewpager_protocol);
                viewpager_uuid = itemView.findViewById(R.id.viewpager_uuid);
            }
        }
    }
}