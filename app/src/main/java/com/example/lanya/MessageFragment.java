package com.example.lanya;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

    private ViewPager2 viewpager2;
    private Button addButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        viewpager2 = view.findViewById(R.id.viewPager);
        addButton = view.findViewById(R.id.addButton);
        CustomPagerAdapter adapter = new CustomPagerAdapter();
        viewpager2.setAdapter(adapter);
        addButton.setOnClickListener(v -> {
            // 动态添加新的布局
            adapter.addItem("新添加的布局");
        });

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

    public class CustomPagerAdapter extends RecyclerView.Adapter<CustomPagerAdapter.CustomViewHolder> {

        private List<String> itemList;

        private int MaxNum = 8;

        public CustomPagerAdapter() {
            this.itemList = new ArrayList<>();
        }

        public void addItem(String text) {
            if (MaxNum == itemList.size())
            {
                Toast.makeText(getContext(), "已经添加8个，不能再添加了", Toast.LENGTH_SHORT).show();
                return;
            }
            itemList.add(text);
            notifyItemInserted(itemList.size() - 1);
        }

        public void deleteItem(String text) {
            int position = itemList.indexOf(text);
            if (position >= 0) {
                itemList.remove(position);
                notifyItemRemoved(position);
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
            // 绑定数据到视图
            //holder.textView.setText(itemList.get(position));
            //holder.textView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            CustomViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}