package com.example.lanya;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
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
import java.util.List;

public class GlobalFragment extends Fragment {
    public Button open_button;
    public Button close_button;
    public RecyclerView recyclerview;
    private final Bluetooth bluetooth;

    public GlobalFragment (Bluetooth bluetooth) {
        this.bluetooth = bluetooth;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        bluetooth.init();
        open_button.setOnClickListener(v -> {
            bluetooth.open(bluetooth.mCustomScanCallback);
            recyclerview.removeAllViews();
        });
        close_button.setOnClickListener(v -> {
            bluetooth.stop();
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(new ArrayList<>(bluetooth.mBluetoothDevice));
            recyclerview.setAdapter(deviceListAdapter);
            recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                holder.deviceAddressTextView.setText(device.getAddress());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mDeviceList.size();
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
                    deviceDialog dialog = new deviceDialog(mDeviceList.get(position),bluetooth);
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