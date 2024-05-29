package com.example.lanya;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lanya.Utils.Bluetooth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalFragment extends Fragment {
    public Button open_button;
    public Button close_button;
    public RecyclerView recyclerview;
    private final Bluetooth mbluetooth;
    public Set<BluetoothDevice> mBluetoothDevice = new HashSet<>();;  // 已被扫描的蓝牙设备（不重复）
    public DeviceListAdapter mdeviceListAdapter;

    public GlobalFragment (Bluetooth bluetooth) {
        this.mbluetooth = bluetooth;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public class Search extends Bluetooth.Search{

        Bluetooth mbluetooth;   // 蓝牙实例
        private BluetoothLeScanner mBluetoothLeScanner;                                                 // 蓝牙搜索实例
        public ScanCallback mScanCallback;                                                              // 全局搜索回调函数对象
        public Search(Bluetooth bluetooth) {
            super(bluetooth);
            this.mbluetooth = bluetooth;
        }

        public ScanCallback mCustomScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                // 添加自定义处理逻辑
                try {
                    BluetoothDevice device = result.getDevice();
                    int rssi = result.getRssi();
                    if (mBluetoothDevice.add(device)){
                        mdeviceListAdapter.addDeviceitem(device);
                        if (device.getName() == null || device.getName().isEmpty()) {
                            Log.i("蓝牙设备", "未知设备：已用MAC代替 [" + device.getAddress() + "]");
                        }
                        else {
                            Log.i("蓝牙设备", "设备名：" + device.getName() + "\tMAC：[" + device.getAddress() + "]\t信号强度：" + rssi);
                        }
                    }
                    else {
                        int index = mdeviceListAdapter.mDeviceList.indexOf(device);
                        Log.i("蓝牙设备","更新广播：设备名：" + device.getName() + "\tMAC：[" + device.getAddress() + "]\t信号强度：" + rssi);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        };

        @Override
        public void start(ScanCallback scanCallback) {
            super.start(scanCallback);

            mdeviceListAdapter = new DeviceListAdapter(new ArrayList<>(mBluetoothDevice));
            recyclerview.setAdapter(mdeviceListAdapter);
            recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

            mScanCallback = scanCallback;
            mBluetoothLeScanner = mbluetooth.mbluetoothAdapter.getBluetoothLeScanner();
            try {
                mBluetoothDevice.clear();
                mdeviceListAdapter.clearData();
                mBluetoothLeScanner.startScan(mScanCallback);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stop() {
            super.stop();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global, container, false);
        open_button = view.findViewById(R.id.open);
        close_button = view.findViewById(R.id.close);
        recyclerview = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Search search = new Search(mbluetooth);
        open_button.setOnClickListener(v -> {
            search.start(search.mCustomScanCallback);
        });
        close_button.setOnClickListener(v -> {
            search.stop();
        });
    }

    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        private List<BluetoothDevice> mDeviceList;

        public DeviceListAdapter(List<BluetoothDevice> deviceList) {
            this.mDeviceList = deviceList;
        }

        /**
         * onCreateViewHolder该函数负责创建单列表位图对象(即ViewHolder类)
         * @param parent RecyclerView的父视图 RecyclerView自身
         * @param viewType viewType表示列表项的类型 一般为自己写的ViewHolder类实现
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.target, parent, false);
            return new ViewHolder(view);
        }

        /**
         * onBindViewHolder该函数负责把onCreateViewHolder传递的单列表位图对象和数据进行绑定
         * @param holder 当前列表的单列表位图对象(即ViewHolder类)
         * @param position 当前列表在第几个项
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BluetoothDevice device = mDeviceList.get(position);
            try {
                if (device.getName() == null || device.getName().isEmpty()) {
                    holder.deviceNameTextView.setText("未知设备");
                }
                else {
                    holder.deviceNameTextView.setText(device.getName());
                }
                holder.deviceAddressTextView.setText("MAC:" + device.getAddress());
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

        /**
         * RecyclerView瀑布流单列表位图对象
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView deviceNameTextView;
            TextView deviceAddressTextView;
            CardView cardView;
            ViewHolder(View itemView) {
                super(itemView);
                deviceNameTextView = itemView.findViewById(R.id.Name);
                deviceAddressTextView = itemView.findViewById(R.id.Address);
                cardView = itemView.findViewById(R.id.DevicewItem);
                cardView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    deviceDialog dialog = new deviceDialog(mDeviceList.get(position),mbluetooth);
                    dialog.show(getChildFragmentManager(), "Message");
                });
            }
        }
    }

    public static class deviceDialog extends DialogFragment {
        BluetoothDevice device;
        Bluetooth bluetooth;
        public deviceDialog(BluetoothDevice device,Bluetooth bluetooth){
            this.device = device;
            this.bluetooth = bluetooth;
        }
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            try {
                builder.setMessage(device.getAddress())
                        .setPositiveButton("关闭", (dialog, id) -> {
                            dismiss();
                        })
                        .setNegativeButton("配对", (dialog, id) -> {
                            bluetooth.Pair(device.getAddress());
                        });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            return builder.create();
        }
    }
}