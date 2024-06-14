package com.example.lanya;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lanya.Utils.Bluetooth;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageFragment extends Fragment {

    public Bluetooth mbluetooth;
    private ViewPager2 viewpager2;
    private TabLayout tabLayout;
    private CustomPagerAdapter customPagerAdapter;
    private List<BluetoothGatt> mGattList = new ArrayList<>();
    private int index = -1;

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
        tabLayout = view.findViewById(R.id.tabLayout);

        new TabLayoutMediator(tabLayout, viewpager2,
                (tab, position) -> tab.setCustomView(R.layout.custom_tab)
        ).attach();

        viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 输出当前选中页面的索引
                Log.d("ViewPager", "当前索引: " + position);
                index = position;
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
        Log.i("UI界面","初始化新的蓝牙连接UI");
        new Bluetooth.ConnectedDevice(mbluetooth.mbluetoothAdapter, event.mMac, getActivity(), new Bluetooth.ConnectedDevice.ConnectionCallback() {
            @Override
            public void onConnectSuccess(BluetoothGatt mBluetoothGatt, String PCOTOCOL, List<BluetoothGattService> bluetoothGattServiceList, String SPP_UUID) {
                bluetoothGatt[0] = mBluetoothGatt;
                getActivity().runOnUiThread(() -> {
                    customPagerAdapter.addItem(event.mDevice, bluetoothGatt[0], new Data(PCOTOCOL, bluetoothGattServiceList, SPP_UUID));
                });
            }

            @Override
            public void onBLEConnectSuccess(BluetoothGatt mBluetoothGatt, String PCOTOCOL, List<BluetoothGattService> bluetoothGattServiceList) {
                bluetoothGatt[0] = mBluetoothGatt;
                getActivity().runOnUiThread(() -> {
                    customPagerAdapter.addItem(event.mDevice, bluetoothGatt[0], new Data(PCOTOCOL, bluetoothGattServiceList));
                });
            }

            @Override
            public void onSPPConnectSuccess(BluetoothGatt mBluetoothGatt, String PCOTOCOL, String SPP_UUID) {
                bluetoothGatt[0] = mBluetoothGatt;
                getActivity().runOnUiThread(() -> {
                    customPagerAdapter.addItem(event.mDevice, bluetoothGatt[0], new Data(PCOTOCOL,SPP_UUID));
                });
            }

            @Override
            public void onConnectFailed(BluetoothDevice device) {
                getActivity().runOnUiThread(() -> {
                    customPagerAdapter.deleteItem(device);
                });
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public static class Data {
        public List<BluetoothGattService> bluetoothGattService;
        public String SPP_UUID;
        public String PCOTOCOL;
        public Data(String PCOTOCOL, List<BluetoothGattService> bluetoothGattService, String SPP_UUID) {
            this.PCOTOCOL = PCOTOCOL;
            this.bluetoothGattService = bluetoothGattService;
            this.SPP_UUID = SPP_UUID;
        }
        public Data(String PCOTOCOL, List<BluetoothGattService> bluetoothGattService) {
            this.PCOTOCOL = PCOTOCOL;
            this.bluetoothGattService = bluetoothGattService;
            this.SPP_UUID = null;
        }
        public Data(String PCOTOCOL, String SPP_UUID) {
            this.PCOTOCOL = PCOTOCOL;
            this.bluetoothGattService = null;
            this.SPP_UUID = SPP_UUID;
        }
    }

    public class CustomPagerAdapter extends RecyclerView.Adapter<CustomPagerAdapter.CustomViewHolder> {

        private List<BluetoothDevice> mDeviceList;
        private List<Data> Data_List;
        private int MaxNum = 8;

        public CustomPagerAdapter() {
            this.mDeviceList = new ArrayList<>();
            this.Data_List = new ArrayList<>();
        }

        public void addItem(BluetoothDevice device, BluetoothGatt bluetoothGatt, Data data) {
            if (MaxNum == mDeviceList.size())
            {
                Toast.makeText(getContext(), "已经添加8个，不能再添加了", Toast.LENGTH_SHORT).show();
                return;
            }
            int index = mDeviceList.indexOf(device);
            if (index != -1) {
                mDeviceList.remove(index);
                Data_List.remove(index);
                mGattList.remove(index);
                notifyItemRemoved(index);
            }
            mDeviceList.add(device);
            Data_List.add(data);
            mGattList.add(bluetoothGatt);
            notifyItemInserted(mDeviceList.size() - 1);
            Log.i("UI界面","MessageFragment新的蓝牙连接已添加进UI界面");
        }

        public void deleteItem(BluetoothDevice device) {
            int index = mDeviceList.indexOf(device);
            if (index != -1) {
                mDeviceList.remove(index);
                Data_List.remove(index);
                mGattList.remove(index);
                notifyItemRemoved(index);
                Log.i("UI界面","MessageFragment蓝牙连接已断开，删除UI界面相关内容");
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
            Data data = Data_List.get(position);

            try {
                holder.viewpager_name.setText(device.getName());
                holder.viewpager_protocol.setText("设备协议:  " + data.PCOTOCOL);
                holder.viewpager_uuid.setText("设备地址:  " + device.getAddress());
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            if (Objects.equals(data.PCOTOCOL, "BLE | SPP")) {
                holder.viewpager_SPP_Layout.setVisibility(View.VISIBLE);
                holder.viewpager_SPP_uuid.setText(data.SPP_UUID);
                holder.serviceListAdapter = new serviceListAdapter(data.bluetoothGattService);
                holder.recyclerView.setAdapter(holder.serviceListAdapter);
                holder.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            else if (Objects.equals(data.PCOTOCOL, "BLE")) {
                holder.serviceListAdapter = new serviceListAdapter(data.bluetoothGattService);
                holder.recyclerView.setAdapter(holder.serviceListAdapter);
                holder.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            else if (Objects.equals(data.PCOTOCOL, "SPP")) {
                holder.viewpager_SPP_Layout.setVisibility(View.VISIBLE);
                holder.viewpager_SPP_uuid.setText(data.SPP_UUID);
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
            RecyclerView recyclerView;
            serviceListAdapter serviceListAdapter;
            RelativeLayout viewpager_SPP_Layout;
            TextView viewpager_SPP_uuid;
            CustomViewHolder(View itemView) {
                super(itemView);
                viewpager_name = itemView.findViewById(R.id.viewpager_name);
                viewpager_protocol = itemView.findViewById(R.id.viewpager_protocol);
                viewpager_uuid = itemView.findViewById(R.id.viewpager_uuid);
                recyclerView = itemView.findViewById(R.id.viewpager_recyclerview);
                viewpager_SPP_Layout = itemView.findViewById(R.id.viewpager_SPP_Layout);
                viewpager_SPP_uuid = itemView.findViewById(R.id.viewpager_SPP_uuid);
            }
        }
    }

    public class serviceListAdapter extends RecyclerView.Adapter<serviceListAdapter.ViewHolder> {
        List<BluetoothGattService> mGattService;
        public serviceListAdapter(List<BluetoothGattService> bluetoothGattServiceList){
            this.mGattService = bluetoothGattServiceList;
        }
        @NonNull
        @Override
        public serviceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BluetoothGattService bluetoothGattService = mGattService.get(position);
            UUID serviceUUID = bluetoothGattService.getUuid();
            holder.service_item_uuid.setText(String.valueOf(serviceUUID));
            List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
            holder.characteristicListAdapter = new CharacteristicListAdapter(characteristics);
            holder.service_item_recyclerView.setAdapter(holder.characteristicListAdapter);
            holder.service_item_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        @Override
        public int getItemCount() {
            return mGattService.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView service_item_name;
            TextView service_item_uuid;
            RecyclerView service_item_recyclerView;
            CharacteristicListAdapter characteristicListAdapter;
            LinearLayout service_item_Linear;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                service_item_name = itemView.findViewById(R.id.service_item_name);
                service_item_uuid = itemView.findViewById(R.id.service_item_uuid);
                service_item_recyclerView = itemView.findViewById(R.id.service_item_recyclerView);
                service_item_Linear = itemView.findViewById(R.id.service_item_Linear);
                itemView.setOnClickListener(v -> {
                    service_item_Linear.setVisibility(service_item_Linear.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                });
            }
        }
    }

    public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
            // 设置底部弹窗的内容和逻辑
            return view;
        }
    }

    public static class CharacteristicEvent{
        public BluetoothGattCharacteristic mCharacteristic;
        public BluetoothGatt mBluetoothGatt;
        public String mCharacteristicUUID;
        CharacteristicEvent (BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, String characteristicUUID){
            mBluetoothGatt = bluetoothGatt;
            mCharacteristic = characteristic;
            mCharacteristicUUID = characteristicUUID;
        }
    }


    public class CharacteristicListAdapter extends RecyclerView.Adapter<CharacteristicListAdapter.ViewHolder> {
        List<BluetoothGattCharacteristic> mCharacteristic;
        public CharacteristicListAdapter(List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList){
            this.mCharacteristic = bluetoothGattCharacteristicList;
        }
        @NonNull
        @Override
        public CharacteristicListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.characteristic_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BluetoothGattCharacteristic characteristic = mCharacteristic.get(position);
            UUID characteristicUUID = characteristic.getUuid();
            holder.characteristic_item_uuid.setText(String.valueOf(characteristicUUID));
            String authority = Bluetooth.getCharacteristicProperties(characteristic);
            holder.characteristic_item_authority.setText(authority);
            holder.itemView.setOnClickListener(v -> {
                Log.i("UI界面","点击的特征UUID:" + characteristicUUID);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());
                View bottomSheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.bottom_sheet_layout, null);
                TextView xz1 = bottomSheetView.findViewById(R.id.bottom_sheet_xz1);
                TextView xz2 = bottomSheetView.findViewById(R.id.bottom_sheet_xz2);
                TextView xz3 = bottomSheetView.findViewById(R.id.bottom_sheet_xz3);
                xz1.setOnClickListener(v1 -> {
                    Intent intent = new Intent(getActivity(), commActivity.class);
                    Log.i("UI界面","开始发送characteristicEvent事件");
                    EventBus.getDefault().postSticky(new CharacteristicEvent(mGattList.get(index),characteristic,String.valueOf(characteristicUUID)));
                    startActivity(intent);
                    bottomSheetDialog.hide();
                });
                xz2.setOnClickListener(v1 -> {
                    bottomSheetDialog.hide();
                });
                xz3.setOnClickListener(v1 -> {
                    bottomSheetDialog.hide();
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            });
        }

        @Override
        public int getItemCount() {
            return mCharacteristic.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView characteristic_item_uuid;
            TextView characteristic_item_authority;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                characteristic_item_uuid = itemView.findViewById(R.id.characteristic_item_uuid);
                characteristic_item_authority = itemView.findViewById(R.id.characteristic_item_authority);
            }
        }
    }
}