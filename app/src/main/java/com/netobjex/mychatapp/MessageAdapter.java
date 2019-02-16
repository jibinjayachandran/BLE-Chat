package com.netobjex.mychatapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netobjex.mychatapp.entities.ChatMessage;

import java.util.List;

/**
 * Created by Jibin on 1/27/2019.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{
    private Activity activity;
    private List<ChatMessage> messages;
    LayoutInflater inflater;

    public MessageAdapter(Activity context, List<ChatMessage> objects) {
        this.inflater = LayoutInflater.from(context);
        this.activity = context;
        this.messages = objects;
    }

    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_chat, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position) {
        if(messages.get(position).isMine()){
            holder.llChatLeft.setVisibility(View.GONE);
            holder.llChatRight.setVisibility(View.VISIBLE);
            holder.txt_msg_mine.setText(messages.get(position).getContent());
        }else{
            holder.llChatLeft.setVisibility(View.VISIBLE);
            holder.llChatRight.setVisibility(View.GONE);
            holder.txt_msg.setText(messages.get(position).getContent());
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        LinearLayout llChatRight;
        LinearLayout llChatLeft;
        TextView txt_msg_mine;
        TextView txt_msg;
        public MyViewHolder(View itemView) {
            super(itemView);
            txt_msg_mine = (TextView)itemView.findViewById(R.id.txt_msg_mine);
            txt_msg = (TextView)itemView.findViewById(R.id.txt_msg);
            llChatRight = (LinearLayout)itemView.findViewById(R.id.llChatRight);
            llChatLeft = (LinearLayout)itemView.findViewById(R.id.llChatLeft);
        }
    }
}
