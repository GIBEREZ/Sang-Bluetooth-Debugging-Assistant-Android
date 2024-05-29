package com.example.lanya;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lanya.Utils.Bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JcFragment extends Fragment {
    Bluetooth mbluetooth;
    public RecyclerView Pair_recyclerview;
    public RecyclerView Monitor_recyclerview;

    public JcFragment(Bluetooth bluetooth) {
        mbluetooth = bluetooth;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jc, container, false);
        Pair_recyclerview = view.findViewById(R.id.PD_List);
        Monitor_recyclerview = view.findViewById(R.id.JC_List);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Fragment 可见
        BluetoothPairList bluetoothPairList = new BluetoothPairList();
        Pair_recyclerview.setAdapter(new DeviceListAdapter(new ArrayList<>(bluetoothPairList.getPairList())));
        Pair_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public class BluetoothPairList extends Bluetooth.BluetoothPairList {
        Set<BluetoothDevice> mPairDevice;
        public BluetoothPairList() {
            super();
        }
        public Set<BluetoothDevice> getPairList() {
            try {
                mPairDevice = mbluetooth.mbluetoothAdapter.getBondedDevices();
                if (mPairDevice.size() > 0) {
                    for (BluetoothDevice device : mPairDevice) {
                        // 获取设备名称和地址3
                        String deviceName = device.getName();
                        String deviceAddress = device.getAddress();
                        Log.i("蓝牙功能","配对设备名称：" + deviceName + "\t配对设备地址：" + deviceAddress);
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            return mPairDevice;
        }
    }

    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
        public List<BluetoothDevice> mDeviceList;

        public DeviceListAdapter(List<BluetoothDevice> DeviceList) {
            this.mDeviceList = DeviceList;
        }

        @NonNull
        @Override
        public DeviceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pd_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceListAdapter.ViewHolder holder, int position) {
            BluetoothDevice device = mDeviceList.get(position);
            try {
                if (device.getName() == null || device.getName().isEmpty()) {
                    holder.text1.setText(device.getAddress());
                }
                else {
                    holder.text1.setText(device.getName());
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mDeviceList.size();
        }

        public void addDeviceitem(BluetoothDevice mbluetoothdevice) {
            int position = mDeviceList.size();
            mDeviceList.add(mbluetoothdevice);
            notifyItemInserted(position);
        }

        public void clearData() {
            mDeviceList.clear();
            notifyDataSetChanged(); // 通知 RecyclerView 更新
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(R.id.PD_item_text);
            }
        }
    }
}