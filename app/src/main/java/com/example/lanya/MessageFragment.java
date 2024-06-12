package com.example.lanya;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lanya.Utils.Bluetooth;
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

    Bluetooth mbluetooth;
    private ViewPager2 viewpager2;
    private TabLayout tabLayout;
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
        private List<BluetoothGatt> mGattList;

        private int MaxNum = 8;

        public CustomPagerAdapter() {
            this.mDeviceList = new ArrayList<>();
            this.Data_List = new ArrayList<>();
            this.mGattList = new ArrayList<>();
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
            CustomViewHolder(View itemView) {
                super(itemView);
                viewpager_name = itemView.findViewById(R.id.viewpager_name);
                viewpager_protocol = itemView.findViewById(R.id.viewpager_protocol);
                viewpager_uuid = itemView.findViewById(R.id.viewpager_uuid);
                recyclerView = itemView.findViewById(R.id.viewpager_recyclerview);
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
            UUID serviceUUID = mGattService.get(position).getUuid();
            holder.service_item_uuid.setText(String.valueOf(serviceUUID));
            List<BluetoothGattCharacteristic> characteristics = mGattService.get(position).getCharacteristics();
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

    public static class CharacteristicListAdapter extends RecyclerView.Adapter<CharacteristicListAdapter.ViewHolder> {
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
        }

        @Override
        public int getItemCount() {
            return mCharacteristic.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder{
            TextView characteristic_item_uuid;
            TextView characteristic_item_authority;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                characteristic_item_uuid = itemView.findViewById(R.id.characteristic_item_uuid);
                characteristic_item_authority = itemView.findViewById(R.id.characteristic_item_authority);
                itemView.setOnClickListener(v -> {
                    
                });
            }
        }
    }
}