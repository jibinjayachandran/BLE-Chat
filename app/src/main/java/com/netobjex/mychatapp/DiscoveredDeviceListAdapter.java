package com.netobjex.mychatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netobjex.mychatapp.callbacks.SelectDeviceCallback;

import java.util.ArrayList;

/**
 * Created by Jibin on 1/27/2019.
 */

public class DiscoveredDeviceListAdapter extends RecyclerView.Adapter<DiscoveredDeviceListAdapter.MyViewHolder> {
    Context context;
    ArrayList<String> items;
    LayoutInflater inflater;
    public DiscoveredDeviceListAdapter(Context context, ArrayList<String> items) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.items = items;

    }

    @NonNull
    @Override
    public DiscoveredDeviceListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.device_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveredDeviceListAdapter.MyViewHolder holder, final int position) {
        holder.tvName.setText(items.get(position));
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the device MAC address, which is the last 17 chars in the View
                String info = items.get(position);
                String name=info.substring(info.indexOf("\n"));
                String address = info.substring(info.length() - 17);
                SelectDeviceCallback callback = (SelectDeviceCallback)context;
                callback.onDeviceSelected(name,address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView)itemView.findViewById(R.id.tvName);
        }
    }
}
