package com.example.lanya;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

    private ViewPager2 viewPager2;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        viewPager2 = view.findViewById(R.id.viewPager);
        return view;
    }

    public class CustomPagerAdapter extends RecyclerView.Adapter<CustomPagerAdapter.CustomViewHolder> {

        private List<String> itemList;

        public CustomPagerAdapter() {
            this.itemList = new ArrayList<>();
        }

        public void addItem(String text) {
            itemList.add(text);
            notifyItemInserted(itemList.size() - 1);
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            holder.textView.setText(itemList.get(position));
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            CustomViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
            }
        }
    }
}