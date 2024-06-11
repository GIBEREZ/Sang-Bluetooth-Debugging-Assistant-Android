package com.example.lanya;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lanya.Utils.Bluetooth;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        showPairList();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showPairList();
        }
    }

    private void showPairList(){
        BluetoothPairList bluetoothPairList = new BluetoothPairList();
        Pair_recyclerview.setAdapter(new DeviceListAdapter(new ArrayList<>(bluetoothPairList.getPairList())));
        Pair_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public static class ConnectionEvent {
        public final String mMac;
        public final BluetoothDevice mDevice;
        public ConnectionEvent(String mac, BluetoothDevice device) {
            this.mMac = mac;
            this.mDevice = device;
        }
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

    public static class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
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
                    holder.item_text.setText(device.getAddress());
                }
                else {
                    holder.item_text.setText(device.getName());
                }
                holder.item_mac.setText("Mac:" + device.getAddress());
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            holder.PD_item_connect.setOnClickListener(v -> {
                EventBus.getDefault().post(new ConnectionEvent(device.getAddress(), device));
            });

            holder.PD_item_delete.setOnClickListener(v -> {
                try {
                    // 使用反射获取removeBond方法
                    Method method = device.getClass().getMethod("removeBond", (Class[]) null);
                    // 调用removeBond方法删除配对
                    method.invoke(device, (Object[]) null);
                    // 记录成功的日志信息
                    Log.d("蓝牙功能", "解除配对设备，成功解除配对: " + device.getName());
                } catch (NoSuchMethodException e) {
                    // 捕获方法不存在异常
                    Log.e("蓝牙功能", "解除配对设备，方法不存在: removeBond", e);
                } catch (IllegalAccessException e) {
                    // 捕获方法访问权限异常
                    Log.e("蓝牙功能", "解除配对设备，无法访问方法: removeBond", e);
                } catch (InvocationTargetException e) {
                    // 捕获调用目标异常
                    Log.e("蓝牙功能", "解除配对设备，调用目标异常: removeBond", e);
                } catch (SecurityException e) {
                    // 捕获安全性异常
                    Log.e("蓝牙功能", "解除配对设备，安全性异常: removeBond", e);
                }
                removeItem(mDeviceList.indexOf(device));
            });
        }

        @Override
        public int getItemCount() {
            return mDeviceList.size();
        }

        public void addItem(BluetoothDevice mbluetoothdevice) {
            int position = mDeviceList.size();
            mDeviceList.add(mbluetoothdevice);
            notifyItemInserted(position);
        }

        // 删除指定位置的项目
        public void removeItem(int position) {
            mDeviceList.remove(position);
            notifyItemRemoved(position);
        }

        public void clearData() {
            mDeviceList.clear();
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView item_text;
            TextView item_mac;
            ImageView Image1;
            LinearLayout panel;
            ImageView PD_item_connect;
            ImageView PD_item_delete;
            boolean isUp = false;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                item_text = itemView.findViewById(R.id.PD_item_text);
                Image1 = itemView.findViewById(R.id.Popup);
                panel = itemView.findViewById(R.id.panel);
                item_mac = itemView.findViewById(R.id.PD_item_mac);
                panel.setVisibility(View.GONE);
                PD_item_connect = itemView.findViewById(R.id.PD_item_connect);
                PD_item_delete = itemView.findViewById(R.id.PD_item_delete);

                Image1.setOnClickListener(v -> {
                    if (isUp) {
                        Image1.setBackgroundResource(R.drawable.down);
                        panel.setVisibility(View.GONE);
                    } else {
                        Image1.setBackgroundResource(R.drawable.up);
                        panel.setVisibility(View.VISIBLE);
                    }
                    isUp = !isUp;
                });
            }
        }
    }
}